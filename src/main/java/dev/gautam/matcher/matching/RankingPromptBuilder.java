package dev.gautam.matcher.matching;

import dev.gautam.matcher.common.ModelResponseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RankingPromptBuilder {

    private final PromptTemplate template;
    private final BeanOutputConverter<CandidateAssessment> converter =
            new BeanOutputConverter<>(CandidateAssessment.class);

    public RankingPromptBuilder(@Value("classpath:prompts/rank-candidate.st") Resource promptResource) {
        this.template = new PromptTemplate(promptResource);
    }

    public String render(String jobTitle, String jobDescription, String candidateName, String evidence) {
        return template.render(Map.of(
                "jobTitle", nullToEmpty(jobTitle),
                "jobDescription", nullToEmpty(jobDescription),
                "candidateName", nullToEmpty(candidateName),
                "evidence", nullToEmpty(evidence),
                "format", converter.getFormat()));
    }

    public CandidateAssessment parse(String modelOutput) {
        if (modelOutput == null || modelOutput.isBlank()) {
            throw new ModelResponseException("The ranking model returned an empty response");
        }
        try {
            CandidateAssessment assessment = converter.convert(stripFences(modelOutput));
            if (assessment == null) {
                throw new ModelResponseException("The ranking model returned an empty assessment");
            }
            return sanitize(assessment);
        } catch (ModelResponseException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ModelResponseException("The ranking model returned a response that could not be parsed", ex);
        }
    }

    private CandidateAssessment sanitize(CandidateAssessment assessment) {
        int score = Math.max(0, Math.min(100, assessment.score()));
        return new CandidateAssessment(
                score,
                cleanList(assessment.strengths()),
                cleanList(assessment.gaps()),
                assessment.rationale() == null || assessment.rationale().isBlank()
                        ? "No rationale was provided."
                        : trim(assessment.rationale(), 4_000));
    }

    private static List<String> cleanList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            cleaned.add(trim(value.trim(), 500));
            if (cleaned.size() == 12) {
                break;
            }
        }
        return List.copyOf(cleaned);
    }

    private static String stripFences(String raw) {
        String trimmed = raw.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline >= 0) {
            trimmed = trimmed.substring(firstNewline + 1);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
