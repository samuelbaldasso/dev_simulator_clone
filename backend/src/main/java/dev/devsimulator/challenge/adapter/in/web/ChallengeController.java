package dev.devsimulator.challenge.adapter.in.web;

import dev.devsimulator.challenge.application.ListChallengesUseCase;
import dev.devsimulator.challenge.domain.Difficulty;
import java.util.Arrays;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

  private final ListChallengesUseCase listChallengesUseCase;

  public ChallengeController(ListChallengesUseCase listChallengesUseCase) {
    this.listChallengesUseCase = listChallengesUseCase;
  }

  @GetMapping
  public ChallengePageResponse list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String difficulty) {
    Difficulty difficultyFilter = parseDifficulty(difficulty);
    return ChallengeMapper.toResponse(listChallengesUseCase.list(page, size, difficultyFilter));
  }

  private Difficulty parseDifficulty(String difficulty) {
    if (difficulty == null || difficulty.isBlank()) {
      return null;
    }
    try {
      return Difficulty.valueOf(difficulty.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("difficulty must be one of " + Arrays.toString(Difficulty.values()));
    }
  }
}
