package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.domain.Challenge;

public interface FindChallengeUseCase {

  /** @throws ChallengeNotFoundException when no challenge exists with the given id */
  Challenge findById(Long id);
}
