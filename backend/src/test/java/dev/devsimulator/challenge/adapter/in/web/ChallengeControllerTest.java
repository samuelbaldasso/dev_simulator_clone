package dev.devsimulator.challenge.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.devsimulator.challenge.application.ChallengeNotFoundException;
import dev.devsimulator.challenge.application.ChallengeNotRunnableException;
import dev.devsimulator.challenge.application.FindChallengeUseCase;
import dev.devsimulator.challenge.application.ListChallengesUseCase;
import dev.devsimulator.challenge.application.RunChallengeCodeUseCase;
import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.ExecutionResult;
import dev.devsimulator.challenge.domain.ExecutionResult.TestResult;
import dev.devsimulator.challenge.domain.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChallengeController.class)
class ChallengeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ListChallengesUseCase listChallengesUseCase;
  @MockBean private FindChallengeUseCase findChallengeUseCase;
  @MockBean private RunChallengeCodeUseCase runChallengeCodeUseCase;

  @Test
  void returnsPagedChallenges() throws Exception {
    Challenge challenge =
        new Challenge(1L, "Fix cart total calculation", Difficulty.BEGINNER, 60, "desc", null, null, null);
    when(listChallengesUseCase.list(0, 10, null)).thenReturn(new PageResult<>(List.of(challenge), 0, 10, 1, 1));

    mockMvc
        .perform(get("/api/challenges"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].difficulty").value("BEGINNER"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void passesDifficultyFilterToUseCase() throws Exception {
    when(listChallengesUseCase.list(eq(0), eq(10), eq(Difficulty.STAFF)))
        .thenReturn(new PageResult<>(List.of(), 0, 10, 0, 0));

    mockMvc.perform(get("/api/challenges").param("difficulty", "staff")).andExpect(status().isOk());
  }

  @Test
  void returnsBadRequestForInvalidDifficulty() throws Exception {
    mockMvc.perform(get("/api/challenges").param("difficulty", "nonsense")).andExpect(status().isBadRequest());
  }

  @Test
  void returnsBadRequestForInvalidSize() throws Exception {
    when(listChallengesUseCase.list(eq(0), eq(0), isNull()))
        .thenThrow(new IllegalArgumentException("size must be between 1 and 100"));

    mockMvc.perform(get("/api/challenges").param("size", "0")).andExpect(status().isBadRequest());
  }

  @Test
  void returnsChallengeDetailById() throws Exception {
    Challenge challenge =
        new Challenge(
            1L, "Fix cart total calculation", Difficulty.BEGINNER, 60, "desc", "javascript", "function f() {}",
            "assertEqual(1, 1, \"noop\")");
    when(findChallengeUseCase.findById(1L)).thenReturn(challenge);

    mockMvc
        .perform(get("/api/challenges/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.runnable").value(true))
        .andExpect(jsonPath("$.language").value("javascript"))
        .andExpect(jsonPath("$.starterCode").value("function f() {}"));
  }

  @Test
  void returnsNotFoundForMissingChallenge() throws Exception {
    when(findChallengeUseCase.findById(999L)).thenThrow(new ChallengeNotFoundException(999L));

    mockMvc.perform(get("/api/challenges/999")).andExpect(status().isNotFound());
  }

  @Test
  void runsSubmittedCodeAndReturnsTestResults() throws Exception {
    ExecutionResult result =
        new ExecutionResult(List.of(new TestResult("adds tax", true, null)), "", null, false);
    when(runChallengeCodeUseCase.run(eq(1L), eq("function calculateTotal() {}"))).thenReturn(result);

    mockMvc
        .perform(
            post("/api/challenges/1/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"function calculateTotal() {}\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tests", hasSize(1)))
        .andExpect(jsonPath("$.tests[0].passed").value(true))
        .andExpect(jsonPath("$.timedOut").value(false));
  }

  @Test
  void returnsConflictWhenChallengeIsNotRunnable() throws Exception {
    when(runChallengeCodeUseCase.run(eq(21L), eq("some code")))
        .thenThrow(new ChallengeNotRunnableException(21L));

    mockMvc
        .perform(
            post("/api/challenges/21/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"some code\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void returnsBadRequestWhenCodeIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/challenges/1/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
