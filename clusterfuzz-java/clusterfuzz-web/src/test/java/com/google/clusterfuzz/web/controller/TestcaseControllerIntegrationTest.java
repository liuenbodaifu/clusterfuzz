package com.google.clusterfuzz.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.clusterfuzz.core.entity.Testcase;
import com.google.clusterfuzz.core.repository.TestcaseRepository;
import com.google.clusterfuzz.web.dto.TestcaseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TestcaseController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class TestcaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestcaseRepository testcaseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Testcase testTestcase;

    @BeforeEach
    void setUp() {
        testcaseRepository.deleteAll();
        
        testTestcase = new Testcase("Heap-buffer-overflow", "main\ntest_function\n", "libfuzzer", "test_job");
        testTestcase.setProjectName("test_project");
        testTestcase.setPlatform("linux");
        testTestcase.setSecurityFlag(true);
        testTestcase.setSecuritySeverity(3);
        testTestcase = testcaseRepository.save(testTestcase);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTestcases_ShouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/v1/testcases")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testTestcase.getId().intValue())))
                .andExpect(jsonPath("$.content[0].crashType", is("Heap-buffer-overflow")))
                .andExpect(jsonPath("$.content[0].fuzzerName", is("libfuzzer")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTestcaseById_ShouldReturnTestcase() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/{id}", testTestcase.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testTestcase.getId().intValue())))
                .andExpect(jsonPath("$.crashType", is("Heap-buffer-overflow")))
                .andExpect(jsonPath("$.crashState", is("main\ntest_function\n")))
                .andExpect(jsonPath("$.fuzzerName", is("libfuzzer")))
                .andExpect(jsonPath("$.jobType", is("test_job")))
                .andExpect(jsonPath("$.securityFlag", is(true)))
                .andExpect(jsonPath("$.securitySeverity", is(3)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTestcaseById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTestcase_ShouldCreateAndReturnTestcase() throws Exception {
        TestcaseDto newTestcase = new TestcaseDto();
        newTestcase.setCrashType("Use-after-free");
        newTestcase.setCrashState("malloc\nfree\n");
        newTestcase.setFuzzerName("afl");
        newTestcase.setJobType("afl_asan_test");
        newTestcase.setProjectName("test_project");
        newTestcase.setPlatform("linux");

        mockMvc.perform(post("/api/v1/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTestcase)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.crashType", is("Use-after-free")))
                .andExpect(jsonPath("$.crashState", is("malloc\nfree\n")))
                .andExpect(jsonPath("$.fuzzerName", is("afl")))
                .andExpect(jsonPath("$.jobType", is("afl_asan_test")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTestcase_WithoutAdminRole_ShouldReturn403() throws Exception {
        TestcaseDto newTestcase = new TestcaseDto();
        newTestcase.setCrashType("Use-after-free");
        newTestcase.setFuzzerName("afl");
        newTestcase.setJobType("afl_asan_test");

        mockMvc.perform(post("/api/v1/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTestcase)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTestcase_ShouldUpdateAndReturnTestcase() throws Exception {
        TestcaseDto updateDto = new TestcaseDto();
        updateDto.setCrashType("Updated-crash-type");
        updateDto.setCrashState("updated\nstate\n");
        updateDto.setFuzzerName("libfuzzer");
        updateDto.setJobType("test_job");
        updateDto.setSecurityFlag(false);

        mockMvc.perform(put("/api/v1/testcases/{id}", testTestcase.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testTestcase.getId().intValue())))
                .andExpect(jsonPath("$.crashType", is("Updated-crash-type")))
                .andExpect(jsonPath("$.crashState", is("updated\nstate\n")))
                .andExpect(jsonPath("$.securityFlag", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTestcase_ShouldDeleteTestcase() throws Exception {
        mockMvc.perform(delete("/api/v1/testcases/{id}", testTestcase.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/testcases/{id}", testTestcase.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchTestcases_ShouldReturnMatchingResults() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/search")
                .param("query", "Heap-buffer-overflow"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].crashType", is("Heap-buffer-overflow")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getOpenTestcases_ShouldReturnOpenTestcases() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/open"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].open", is(true)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSecurityBugs_ShouldReturnSecurityTestcases() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/security"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].securityFlag", is(true)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTestcasesByProject_ShouldReturnProjectTestcases() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/project/{projectName}", "test_project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectName", is("test_project")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTestcasesByFuzzer_ShouldReturnFuzzerTestcases() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/fuzzer/{fuzzerName}", "libfuzzer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fuzzerName", is("libfuzzer")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsTriaged_ShouldUpdateTriageStatus() throws Exception {
        mockMvc.perform(post("/api/v1/testcases/{id}/triage", testTestcase.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.triaged", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeTestcase_ShouldCloseTestcase() throws Exception {
        mockMvc.perform(post("/api/v1/testcases/{id}/close", testTestcase.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.open", is(false)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reopenTestcase_ShouldReopenTestcase() throws Exception {
        // First close the testcase
        mockMvc.perform(post("/api/v1/testcases/{id}/close", testTestcase.getId()));

        // Then reopen it
        mockMvc.perform(post("/api/v1/testcases/{id}/reopen", testTestcase.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.open", is(true)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTestcaseStats_ShouldReturnStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/testcases/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTestcases", is(1)))
                .andExpect(jsonPath("$.openTestcases", is(1)))
                .andExpect(jsonPath("$.securityBugs", is(1)));
    }

    @Test
    void accessWithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/testcases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessWithBasicAuth_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/v1/testcases")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }
}