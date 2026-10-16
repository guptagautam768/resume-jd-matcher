package dev.gautam.matcher.job;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.gautam.matcher.common.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JobControllerTest {

    @Test
    void rejectsABlankTitle() throws Exception {
        var mockMvc = MockMvcBuilders.standaloneSetup(new JobController(mock(JobService.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  \",\"description\":\"Build Java services\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
