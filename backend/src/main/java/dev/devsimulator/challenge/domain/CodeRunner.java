package dev.devsimulator.challenge.domain;

/** Port for executing untrusted challenge code in an isolated sandbox. */
public interface CodeRunner {

  ExecutionResult run(String language, String userCode, String testCode);
}
