package dev.devsimulator.challenge.adapter.in.web;

import java.util.List;

public record ChallengePageResponse(
    List<ChallengeResponse> content, int page, int size, long totalElements, int totalPages) {}
