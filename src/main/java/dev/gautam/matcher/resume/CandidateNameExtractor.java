package dev.gautam.matcher.resume;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class CandidateNameExtractor {

    public String fromText(String text, String fallback) {
        if (text == null || text.isBlank()) {
            return cleanFallback(fallback);
        }
        for (String line : text.split("\\R")) {
            String trimmed = line.replaceAll("\\s+", " ").trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            trimmed = trimmed.replaceFirst("(?i)^name\\s*[:\\-]\\s*", "");
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > 120) {
                trimmed = trimmed.substring(0, 120).trim();
            }
            return trimmed;
        }
        return cleanFallback(fallback);
    }

    private String cleanFallback(String fallback) {
        if (fallback == null || fallback.isBlank()) {
            return "Unknown candidate";
        }
        String name = fallback;
        if (name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            name = name.substring(0, name.length() - 4);
        }
        name = name.replace('-', ' ').replace('_', ' ').trim();
        if (name.isEmpty()) {
            return "Unknown candidate";
        }
        return name.length() > 120 ? name.substring(0, 120) : name;
    }
}
