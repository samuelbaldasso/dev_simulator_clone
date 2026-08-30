package dev.devsimulator.challenge.application;

public class ChallengeNotRunnableException extends RuntimeException {

  public ChallengeNotRunnableException(Long id) {
    super("Challenge has no runnable code: " + id);
  }
}
