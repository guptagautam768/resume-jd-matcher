package dev.gautam.matcher.resume;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "resume")
public class Resume {

    @Id
    private UUID id;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "raw_text", nullable = false)
    private String rawText;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Resume() {
    }

    public Resume(UUID id, String candidateName, String fileName, String rawText) {
        this.id = id;
        this.candidateName = candidateName;
        this.fileName = fileName;
        this.rawText = rawText;
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

    public String getCandidateName() {
        return candidateName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRawText() {
        return rawText;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
