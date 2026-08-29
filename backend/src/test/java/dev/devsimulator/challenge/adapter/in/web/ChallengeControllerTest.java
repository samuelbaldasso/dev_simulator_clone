package dev.devsimulator.challenge.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.devsimulator.challenge.application.ListChallengesUseCase;
import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChallengeController.class)
class ChallengeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ListChallengesUseCase listChallengesUseCase;

  @Test
  void returnsPagedChallenges() throws Exception {
    Challenge challenge = new Challenge(1L, "Fix cart total calculation", Difficulty.BEGINNER, 60, "desc");
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
}
