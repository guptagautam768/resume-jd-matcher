package dev.gautam.matcher.job;

import dev.gautam.matcher.common.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse create(CreateJobRequest request) {
        JobDescription job = jobRepository.save(new JobDescription(
                UUID.randomUUID(),
                request.title().trim(),
                request.description().trim()));
        return JobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> list() {
        return jobRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(JobResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponse get(UUID id) {
        return JobResponse.from(require(id));
    }

    public JobDescription require(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found: " + id));
    }
}
