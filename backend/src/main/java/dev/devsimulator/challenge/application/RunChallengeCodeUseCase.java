package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.domain.ExecutionResult;

public interface RunChallengeCodeUseCase {

  /**
   * @throws ChallengeNotFoundException when no challenge exists with the given id
   * @throws ChallengeNotRunnableException when the challenge has no runnable code (design-only)
   */
  ExecutionResult run(Long challengeId, String submittedCode);
}
