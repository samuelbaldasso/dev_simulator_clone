package dev.devsimulator.challenge.domain;

public interface ChallengeRepository {

  /** {@code difficulty} is nullable: null means no filtering by difficulty. */
  PageResult<Challenge> findAll(int page, int size, Difficulty difficulty);
}
