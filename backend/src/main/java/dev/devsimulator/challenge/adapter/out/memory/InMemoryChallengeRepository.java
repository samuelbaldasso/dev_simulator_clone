package dev.devsimulator.challenge.adapter.out.memory;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.ChallengeRepository;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChallengeRepository implements ChallengeRepository {

  private final List<Challenge> challenges = ChallengeSeeder.seed();

  @Override
  public PageResult<Challenge> findAll(int page, int size, Difficulty difficulty) {
    List<Challenge> filtered =
        difficulty == null
            ? challenges
            : challenges.stream().filter(challenge -> challenge.difficulty() == difficulty).toList();

    int totalElements = filtered.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    int fromIndex = Math.min(page * size, totalElements);
    int toIndex = Math.min(fromIndex + size, totalElements);
    List<Challenge> content = filtered.subList(fromIndex, toIndex);
    return new PageResult<>(content, page, size, totalElements, totalPages);
  }

  @Override
  public Optional<Challenge> findById(Long id) {
    return challenges.stream().filter(challenge -> challenge.id().equals(id)).findFirst();
  }
}
