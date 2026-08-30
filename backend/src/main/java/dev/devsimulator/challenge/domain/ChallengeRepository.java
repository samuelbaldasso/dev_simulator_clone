package dev.devsimulator.challenge.domain;

import java.util.Optional;

public interface ChallengeRepository {

  /** {@code difficulty} is nullable: null means no filtering by difficulty. */
  PageResult<Challenge> findAll(int page, int size, Difficulty difficulty);

  Optional<Challenge> findById(Long id);
}
