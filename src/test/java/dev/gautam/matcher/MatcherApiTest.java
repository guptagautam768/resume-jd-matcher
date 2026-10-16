package dev.gautam.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.gautam.matcher.demo.HashEmbeddingModel;
import dev.gautam.matcher.demo.HeuristicRankChatModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class MatcherApiTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ToolCallbackProvider matcherToolCallbacks;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE match_result, job_description, resume, vector_store");
    }

    @Test
    void healthAndHomePageAreAvailable() throws Exception {
        assertThat(embeddingModel).isInstanceOf(HashEmbeddingModel.class);
        assertThat(chatModel).isInstanceOf(HeuristicRankChatModel.class);

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Resume")));
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ranksABackendResumeAheadOfAFrontendResumeAndDeletesVectors() throws Exception {
        mockMvc.perform(multipart("/api/resumes")
                        .file(pdf("samples/resumes/01-priya-natarajan.pdf"))
                        .file(pdf("samples/resumes/02-diego-alvarez.pdf")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].candidateName").value("Priya Natarajan"))
                .andExpect(jsonPath("$[1].candidateName").value("Diego Alvarez"));

        String jobId = createJob(Path.of("samples/jobs/senior-backend-engineer.json"));

        mockMvc.perform(post("/api/jobs/{id}/match", jobId).param("topK", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Priya Natarajan"))
                .andExpect(jsonPath("$[0].strengths[0]").exists())
                .andExpect(jsonPath("$[0].gaps").isArray())
                .andExpect(jsonPath("$[0].rationale").isNotEmpty())
                .andExpect(jsonPath("$[1].candidateName").value("Diego Alvarez"));

        mockMvc.perform(get("/api/jobs/{id}/matches", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Priya Natarajan"));

        String resumeId = jdbc.queryForObject(
                "select id::text from resume where candidate_name = 'Priya Natarajan'", String.class);
        Integer chunks = jdbc.queryForObject(
                "select count(*) from vector_store where metadata->>'resumeId' = ?",
                Integer.class,
                resumeId);
        assertThat(chunks).isGreaterThan(0);

        mockMvc.perform(get("/api/jobs/{jobId}/matches/{resumeId}", jobId, resumeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateName").value("Priya Natarajan"));

        mockMvc.perform(delete("/api/resumes/{id}", resumeId))
                .andExpect(status().isNoContent());
        Integer remaining = jdbc.queryForObject(
                "select count(*) from vector_store where metadata->>'resumeId' = ?",
                Integer.class,
                resumeId);
        assertThat(remaining).isZero();
        mockMvc.perform(get("/api/resumes/{id}", resumeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void ranksAFrontendResumeFirstForAFrontendJob() throws Exception {
        mockMvc.perform(multipart("/api/resumes")
                        .file(pdf("samples/resumes/02-diego-alvarez.pdf"))
                        .file(pdf("samples/resumes/01-priya-natarajan.pdf")))
                .andExpect(status().isCreated());
        String jobId = createJob(Path.of("samples/jobs/frontend-engineer.json"));

        mockMvc.perform(post("/api/jobs/{id}/match", jobId).param("topK", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Diego Alvarez"))
                .andExpect(jsonPath("$[1].candidateName").value("Priya Natarajan"));
    }

    @Test
    void rejectsANonPdfAndAnUnknownJob() throws Exception {
        MockMultipartFile text = new MockMultipartFile(
                "files", "notes.txt", "text/plain", "not a resume".getBytes());
        mockMvc.perform(multipart("/api/resumes").file(text))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/jobs/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/jobs/00000000-0000-0000-0000-000000000000/match"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exposesTheMcpTools() {
        Set<String> names = Arrays.stream(matcherToolCallbacks.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertThat(names).contains(
                "upload_resume_text",
                "create_job",
                "match_candidates",
                "explain_match",
                "list_resumes");
    }

    private String createJob(Path path) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Files.readString(path)))
                .andExpect(status().isCreated())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private static MockMultipartFile pdf(String path) throws Exception {
        Path file = Path.of(path);
        return new MockMultipartFile(
                "files",
                file.getFileName().toString(),
                "application/pdf",
                Files.readAllBytes(file));
    }
}
