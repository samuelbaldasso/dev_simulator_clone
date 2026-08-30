package dev.devsimulator.challenge.domain;

/**
 * {@code language}, {@code starterCode} and {@code testCode} are null for design-only
 * challenges (e.g. STAFF-level architecture tasks) that have no runnable code.
 */
public record Challenge(
    Long id,
    String title,
    Difficulty difficulty,
    int xp,
    String description,
    String language,
    String starterCode,
    String testCode) {

  public boolean isRunnable() {
    return language != null && starterCode != null && testCode != null;
  }
}
