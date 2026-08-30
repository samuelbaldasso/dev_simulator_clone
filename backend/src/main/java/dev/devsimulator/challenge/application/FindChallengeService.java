package dev.devsimulator.challenge.application;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.ChallengeRepository;
import org.springframework.stereotype.Service;

@Service
public class FindChallengeService implements FindChallengeUseCase {

  private final ChallengeRepository challengeRepository;

  public FindChallengeService(ChallengeRepository challengeRepository) {
    this.challengeRepository = challengeRepository;
  }

  @Override
  public Challenge findById(Long id) {
    return challengeRepository.findById(id).orElseThrow(() -> new ChallengeNotFoundException(id));
  }
}
