package dev.devsimulator.challenge.adapter.in.web;

import dev.devsimulator.challenge.application.FindChallengeUseCase;
import dev.devsimulator.challenge.application.ListChallengesUseCase;
import dev.devsimulator.challenge.application.RunChallengeCodeUseCase;
import dev.devsimulator.challenge.domain.Difficulty;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

  private final ListChallengesUseCase listChallengesUseCase;
  private final FindChallengeUseCase findChallengeUseCase;
  private final RunChallengeCodeUseCase runChallengeCodeUseCase;

  public ChallengeController(
      ListChallengesUseCase listChallengesUseCase,
      FindChallengeUseCase findChallengeUseCase,
      RunChallengeCodeUseCase runChallengeCodeUseCase) {
    this.listChallengesUseCase = listChallengesUseCase;
    this.findChallengeUseCase = findChallengeUseCase;
    this.runChallengeCodeUseCase = runChallengeCodeUseCase;
  }

  @GetMapping
  public ChallengePageResponse list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String difficulty) {
    Difficulty difficultyFilter = parseDifficulty(difficulty);
    return ChallengeMapper.toResponse(listChallengesUseCase.list(page, size, difficultyFilter));
  }

  @GetMapping("/{id}")
  public ChallengeDetailResponse findById(@PathVariable Long id) {
    return ChallengeMapper.toDetailResponse(findChallengeUseCase.findById(id));
  }

  @PostMapping("/{id}/run")
  public RunCodeResponse run(@PathVariable Long id, @Valid @RequestBody RunCodeRequest request) {
    return ChallengeMapper.toResponse(runChallengeCodeUseCase.run(id, request.code()));
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
