package dev.gautam.matcher.matching;

import dev.gautam.matcher.job.JobDescription;
import dev.gautam.matcher.resume.Resume;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "match_result")
public class MatchResult {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobDescription job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal similarity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> strengths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> gaps = new ArrayList<>();

    @Column(nullable = false)
    private String rationale;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected MatchResult() {
    }

    public MatchResult(
            UUID id,
            JobDescription job,
            Resume resume,
            BigDecimal score,
            BigDecimal similarity,
            List<String> strengths,
            List<String> gaps,
            String rationale) {
        this.id = id;
        this.job = job;
        this.resume = resume;
        this.score = score;
        this.similarity = similarity;
        this.strengths = strengths;
        this.gaps = gaps;
        this.rationale = rationale;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public UUID getId() {
        return id;
    }

    public JobDescription getJob() {
        return job;
    }

    public Resume getResume() {
        return resume;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getSimilarity() {
        return similarity;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public List<String> getGaps() {
        return gaps;
    }

    public String getRationale() {
        return rationale;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
