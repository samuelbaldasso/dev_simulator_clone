package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;

public interface ListChallengesUseCase {

  /** {@code difficulty} is nullable: null means no filtering by difficulty. */
  PageResult<Challenge> list(int page, int size, Difficulty difficulty);
}
