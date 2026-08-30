package dev.devsimulator.challenge.adapter.in.web;

import java.util.List;

public record RunCodeResponse(List<TestResultResponse> tests, String consoleOutput, String error, boolean timedOut) {

  public record TestResultResponse(String name, boolean passed, String message) {}
}
