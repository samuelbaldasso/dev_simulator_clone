package dev.devsimulator.challenge.domain;

public record Challenge(Long id, String title, Difficulty difficulty, int xp, String description) {}
