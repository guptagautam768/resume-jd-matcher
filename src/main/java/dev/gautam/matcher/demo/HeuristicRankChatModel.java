package dev.gautam.matcher.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * Scores a candidate from skill phrases that appear in both the job text and the resume evidence.
 * This is a local stand-in so the pipeline can run without an API key. It is not an LLM.
 */
public final class HeuristicRankChatModel implements ChatModel {

    static final List<String> SKILLS = List.of(
            "Spring Boot",
            "Spring Data JPA",
            "PostgreSQL",
            "JavaScript",
            "TypeScript",
            "Kubernetes",
            "microservices",
            "Hibernate",
            "GraphQL",
            "Next.js",
            "FastAPI",
            "PyTorch",
            "Airflow",
            "Terraform",
            "Docker",
            "Flyway",
            "Kafka",
            "JUnit",
            "React",
            "Python",
            "Spark",
            "MySQL",
            "Java",
            "REST",
            "AWS",
            "CSS",
            "Go");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse call(Prompt prompt) {
        String text = prompt.getContents() == null ? "" : prompt.getContents();
        String job = section(text, "Job description:", "Candidate:");
        String evidence = section(text, "Resume evidence:", "Give an integer score");
        if (evidence.isBlank()) {
            evidence = text;
        }
        List<String> inBoth = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : SKILLS) {
            boolean inJob = containsTerm(job, skill);
            boolean inEvidence = containsTerm(evidence, skill);
            if (inJob && inEvidence) {
                inBoth.add(skill);
            } else if (inJob) {
                missing.add(skill);
            }
        }
        int jobSkillCount = inBoth.size() + missing.size();
        int score = jobSkillCount == 0 ? 40 : (int) Math.round(100.0 * inBoth.size() / jobSkillCount);
        List<String> strengths = inBoth.isEmpty()
                ? List.of("No overlapping skills were found in the supplied evidence")
                : List.copyOf(inBoth);
        List<String> gaps = missing.isEmpty()
                ? List.of("No missing required skills were detected from the skill list")
                : List.copyOf(missing.subList(0, Math.min(6, missing.size())));
        String rationale = inBoth.isEmpty()
                ? "The supplied evidence does not mention the skills called out in the job description."
                : "The supplied evidence mentions " + String.join(", ", strengths)
                        + (missing.isEmpty() ? "." : ", and does not mention " + String.join(", ", gaps) + ".");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("score", score);
        body.put("strengths", strengths);
        body.put("gaps", gaps);
        body.put("rationale", rationale);
        try {
            String json = objectMapper.writeValueAsString(body);
            return new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not encode heuristic assessment", ex);
        }
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.just(call(prompt));
    }

    static boolean containsTerm(String text, String term) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String haystack = text.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String needle = term.toLowerCase(Locale.ROOT);
        int from = 0;
        while (from < haystack.length()) {
            int index = haystack.indexOf(needle, from);
            if (index < 0) {
                return false;
            }
            boolean leftOk = index == 0 || !Character.isLetterOrDigit(haystack.charAt(index - 1));
            int end = index + needle.length();
            boolean rightOk = end >= haystack.length() || !Character.isLetterOrDigit(haystack.charAt(end));
            if (leftOk && rightOk) {
                return true;
            }
            from = index + 1;
        }
        return false;
    }

    private static String section(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start < 0) {
            return "";
        }
        start += startMarker.length();
        int end = text.indexOf(endMarker, start);
        if (end < 0) {
            end = text.length();
        }
        return text.substring(start, end).trim();
    }
}
