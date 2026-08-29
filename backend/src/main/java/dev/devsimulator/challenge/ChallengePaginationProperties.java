package dev.devsimulator.challenge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "challenges.pagination")
public record ChallengePaginationProperties(int maxSize) {

  public ChallengePaginationProperties {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("challenges.pagination.max-size must be > 0");
    }
  }
}
