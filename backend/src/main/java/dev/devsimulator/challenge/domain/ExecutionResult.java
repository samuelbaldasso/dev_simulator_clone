package dev.devsimulator.challenge.domain;

import java.util.List;

/**
 * {@code timedOut} and {@code error} are mutually informative: a timeout always leaves
 * {@code tests} empty, while a caught script error may still report tests that ran before it.
 */
public record ExecutionResult(List<TestResult> tests, String consoleOutput, String error, boolean timedOut) {

  public record TestResult(String name, boolean passed, String message) {}
}
