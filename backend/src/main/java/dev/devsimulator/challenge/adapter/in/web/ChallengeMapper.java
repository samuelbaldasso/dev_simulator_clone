package dev.devsimulator.challenge.adapter.in.web;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.PageResult;
import java.util.List;

final class ChallengeMapper {

  private ChallengeMapper() {}

  static ChallengeResponse toResponse(Challenge challenge) {
    return new ChallengeResponse(
        challenge.id(), challenge.title(), challenge.difficulty().name(), challenge.xp(), challenge.description());
  }

  static ChallengePageResponse toResponse(PageResult<Challenge> pageResult) {
    List<ChallengeResponse> content = pageResult.content().stream().map(ChallengeMapper::toResponse).toList();
    return new ChallengePageResponse(
        content, pageResult.page(), pageResult.size(), pageResult.totalElements(), pageResult.totalPages());
  }
}
