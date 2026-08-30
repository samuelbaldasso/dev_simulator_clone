package dev.devsimulator.challenge.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.devsimulator.challenge.ChallengePaginationProperties;
import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.ChallengeRepository;
import dev.devsimulator.challenge.domain.Difficulty;
import dev.devsimulator.challenge.domain.PageResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListChallengesServiceTest {

  private static final ChallengePaginationProperties PROPERTIES = new ChallengePaginationProperties(50);

  @Mock private ChallengeRepository challengeRepository;

  private ListChallengesService service;

  @BeforeEach
  void setUp() {
    service = new ListChallengesService(challengeRepository, PROPERTIES);
  }

  @Test
  void rejectsNegativePage() {
    assertThatThrownBy(() -> service.list(-1, 10, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("page");
  }

  @Test
  void rejectsZeroSize() {
    assertThatThrownBy(() -> service.list(0, 0, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("size");
  }

  @Test
  void rejectsSizeAboveConfiguredMax() {
    assertThatThrownBy(() -> service.list(0, 51, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("50");
  }

  @Test
  void delegatesToRepositoryWithGivenArgumentsWhenValid() {
    Challenge challenge = new Challenge(1L, "Fix bug", Difficulty.BEGINNER, 60, "desc", null, null, null);
    PageResult<Challenge> expected = new PageResult<>(List.of(challenge), 0, 10, 1, 1);
    when(challengeRepository.findAll(0, 10, Difficulty.BEGINNER)).thenReturn(expected);

    PageResult<Challenge> result = service.list(0, 10, Difficulty.BEGINNER);

    assertThat(result).isEqualTo(expected);
    verify(challengeRepository).findAll(eq(0), eq(10), eq(Difficulty.BEGINNER));
  }

  @Test
  void allowsNullDifficultyAsNoFilter() {
    when(challengeRepository.findAll(any(Integer.class), any(Integer.class), eq(null)))
        .thenReturn(new PageResult<>(List.of(), 0, 10, 0, 0));

    service.list(0, 10, null);

    verify(challengeRepository).findAll(0, 10, null);
  }
}
