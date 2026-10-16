package dev.gautam.matcher.matching;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/{jobId}")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/match")
    public List<MatchResponse> match(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "5") int topK) {
        return matchService.match(jobId, topK);
    }

    @GetMapping("/matches")
    public List<MatchResponse> list(@PathVariable UUID jobId) {
        return matchService.list(jobId);
    }

    @GetMapping("/matches/{resumeId}")
    public MatchResponse get(@PathVariable UUID jobId, @PathVariable UUID resumeId) {
        return matchService.explain(jobId, resumeId);
    }
}
