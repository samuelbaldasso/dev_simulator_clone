package dev.devsimulator.challenge.adapter.out.memory;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;
import org.junit.jupiter.api.Test;

class InMemoryChallengeRepositoryTest {

  private final InMemoryChallengeRepository repository = new InMemoryChallengeRepository();

  @Test
  void returnsFirstPageWithoutFilter() {
    PageResult<Challenge> result = repository.findAll(0, 5, null);

    assertThat(result.content()).hasSize(5);
    assertThat(result.totalElements()).isEqualTo(25);
    assertThat(result.totalPages()).isEqualTo(5);
  }

  @Test
  void returnsEmptyContentWhenPageIsPastTheEnd() {
    PageResult<Challenge> result = repository.findAll(10, 5, null);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(25);
  }

  @Test
  void filtersByDifficulty() {
    PageResult<Challenge> result = repository.findAll(0, 25, Difficulty.STAFF);

    assertThat(result.totalElements()).isEqualTo(5);
    assertThat(result.content()).allSatisfy(challenge -> assertThat(challenge.difficulty()).isEqualTo(Difficulty.STAFF));
  }

  @Test
  void paginatesWithinFilteredResults() {
    PageResult<Challenge> result = repository.findAll(0, 2, Difficulty.JUNIOR);

    assertThat(result.content()).hasSize(2);
    assertThat(result.totalElements()).isEqualTo(5);
    assertThat(result.totalPages()).isEqualTo(3);
  }
}
