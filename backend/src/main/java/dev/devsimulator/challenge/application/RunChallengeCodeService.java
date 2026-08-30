package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.ChallengeRepository;
import dev.devsimulator.challenge.domain.CodeRunner;
import dev.devsimulator.challenge.domain.ExecutionResult;
import org.springframework.stereotype.Service;

@Service
public class RunChallengeCodeService implements RunChallengeCodeUseCase {

  private static final int MAX_CODE_LENGTH = 20_000;

  private final ChallengeRepository challengeRepository;
  private final CodeRunner codeRunner;

  public RunChallengeCodeService(ChallengeRepository challengeRepository, CodeRunner codeRunner) {
    this.challengeRepository = challengeRepository;
    this.codeRunner = codeRunner;
  }

  @Override
  public ExecutionResult run(Long challengeId, String submittedCode) {
    if (submittedCode == null || submittedCode.isBlank()) {
      throw new IllegalArgumentException("code must not be blank");
    }
    if (submittedCode.length() > MAX_CODE_LENGTH) {
      throw new IllegalArgumentException("code must not exceed " + MAX_CODE_LENGTH + " characters");
    }

    Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(challengeId));
    if (!challenge.isRunnable()) {
      throw new ChallengeNotRunnableException(challengeId);
    }

    return codeRunner.run(challenge.language(), submittedCode, challenge.testCode());
  }
}
