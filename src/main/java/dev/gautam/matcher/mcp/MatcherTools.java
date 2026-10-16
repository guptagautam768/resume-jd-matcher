package dev.gautam.matcher.mcp;

import dev.gautam.matcher.common.InvalidRequestException;
import dev.gautam.matcher.config.MatcherProperties;
import dev.gautam.matcher.job.CreateJobRequest;
import dev.gautam.matcher.job.JobResponse;
import dev.gautam.matcher.job.JobService;
import dev.gautam.matcher.matching.MatchResponse;
import dev.gautam.matcher.matching.MatchService;
import dev.gautam.matcher.resume.ResumeResponse;
import dev.gautam.matcher.resume.ResumeService;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MatcherTools {

    private final ResumeService resumeService;
    private final JobService jobService;
    private final MatchService matchService;
    private final MatcherProperties properties;

    public MatcherTools(
            ResumeService resumeService,
            JobService jobService,
            MatchService matchService,
            MatcherProperties properties) {
        this.resumeService = resumeService;
        this.jobService = jobService;
        this.matchService = matchService;
        this.properties = properties;
    }

    @Tool(name = "list_resumes", description = "List ingested resumes. Returns id, candidate name, and file name.")
    public List<ResumeSummary> listResumes() {
        return resumeService.list().stream()
                .map(resume -> new ResumeSummary(resume.id(), resume.candidateName(), resume.fileName()))
                .toList();
    }

    @Tool(
            name = "upload_resume_text",
            description = "Ingest a resume from plain text, chunk it, and embed it. Accepts synthetic or user-supplied text only.")
    public ResumeSummary uploadResumeText(
            @ToolParam(description = "Candidate display name", required = true) String candidateName,
            @ToolParam(description = "Full resume text", required = true) String resumeText) {
        ResumeResponse saved = resumeService.uploadText(candidateName, resumeText);
        return new ResumeSummary(saved.id(), saved.candidateName(), saved.fileName());
    }

    @Tool(name = "create_job", description = "Create a job description and return its id.")
    public JobResponse createJob(
            @ToolParam(description = "Job title, up to 200 characters", required = true) String title,
            @ToolParam(description = "Job description, up to 20000 characters", required = true) String description) {
        if (title == null || title.isBlank() || title.trim().length() > 200) {
            throw new InvalidRequestException("title is required and must be 200 characters or fewer");
        }
        if (description == null || description.isBlank() || description.trim().length() > 20_000) {
            throw new InvalidRequestException("description is required and must be 20000 characters or fewer");
        }
        return jobService.create(new CreateJobRequest(title, description));
    }

    @Tool(
            name = "match_candidates",
            description = "Rank ingested resumes against a job. Returns score, similarity, strengths, gaps, and rationale.")
    public List<MatchResponse> matchCandidates(
            @ToolParam(description = "Job id (UUID)", required = true) String jobId,
            @ToolParam(description = "How many candidates to return. Pass 0 to use the default.", required = true) int topK) {
        int resolved = topK <= 0 ? properties.getDefaultTopK() : topK;
        return matchService.match(parseId(jobId, "jobId"), resolved);
    }

    @Tool(
            name = "explain_match",
            description = "Return the stored explanation for one resume against a job. Computes and stores it when none exists.")
    public MatchResponse explainMatch(
            @ToolParam(description = "Job id (UUID)", required = true) String jobId,
            @ToolParam(description = "Resume id (UUID)", required = true) String resumeId) {
        return matchService.explain(parseId(jobId, "jobId"), parseId(resumeId, "resumeId"));
    }

    private static UUID parseId(String value, String label) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException ex) {
            throw new InvalidRequestException(label + " must be a UUID");
        }
    }

    public record ResumeSummary(UUID id, String candidateName, String fileName) {
    }
}
