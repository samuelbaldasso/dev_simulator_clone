package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.ChallengePaginationProperties;
import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.ChallengeRepository;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;
import org.springframework.stereotype.Service;

@Service
public class ListChallengesService implements ListChallengesUseCase {

  private final ChallengeRepository challengeRepository;
  private final ChallengePaginationProperties paginationProperties;

  public ListChallengesService(
      ChallengeRepository challengeRepository, ChallengePaginationProperties paginationProperties) {
    this.challengeRepository = challengeRepository;
    this.paginationProperties = paginationProperties;
  }

  @Override
  public PageResult<Challenge> list(int page, int size, Difficulty difficulty) {
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size <= 0 || size > paginationProperties.maxSize()) {
      throw new IllegalArgumentException("size must be between 1 and " + paginationProperties.maxSize());
    }
    return challengeRepository.findAll(page, size, difficulty);
  }
}
