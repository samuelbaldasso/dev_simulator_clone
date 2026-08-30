package dev.devsimulator.challenge.adapter.in.web;

public record ChallengeDetailResponse(
    Long id,
    String title,
    String difficulty,
    int xp,
    String description,
    boolean runnable,
    String language,
    String starterCode) {}
