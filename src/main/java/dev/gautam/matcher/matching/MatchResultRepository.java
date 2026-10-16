package dev.gautam.matcher.matching;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByJob_IdOrderByScoreDescSimilarityDesc(UUID jobId);

    Optional<MatchResult> findFirstByJob_IdAndResume_IdOrderByCreatedAtDesc(UUID jobId, UUID resumeId);

    void deleteByJob_Id(UUID jobId);
}
