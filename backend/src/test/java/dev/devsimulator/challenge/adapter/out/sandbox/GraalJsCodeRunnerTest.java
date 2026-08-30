package dev.devsimulator.challenge.adapter.out.sandbox;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devsimulator.challenge.domain.ExecutionResult;
import org.junit.jupiter.api.Test;

class GraalJsCodeRunnerTest {

  private final GraalJsCodeRunner runner = new GraalJsCodeRunner();

  @Test
  void reportsPassingAndFailingAssertions() {
    ExecutionResult result =
        runner.run(
            "javascript",
            "function add(a, b) { return a + b; }",
            "assertEqual(add(2, 3), 5, \"adds correctly\");\nassertEqual(add(2, 2), 5, \"deliberately wrong\");");

    assertThat(result.timedOut()).isFalse();
    assertThat(result.error()).isNull();
    assertThat(result.tests()).hasSize(2);
    assertThat(result.tests().get(0).passed()).isTrue();
    assertThat(result.tests().get(1).passed()).isFalse();
    assertThat(result.tests().get(1).message()).contains("expected 5");
  }

  @Test
  void capturesConsoleOutput() {
    ExecutionResult result =
        runner.run("javascript", "console.log('hello', 42);", "assertEqual(1, 1, \"noop\")");

    assertThat(result.consoleOutput()).contains("hello 42");
  }

  @Test
  void reportsScriptErrorsWithoutCrashing() {
    ExecutionResult result = runner.run("javascript", "throw new Error('boom');", "assertEqual(1, 1, \"noop\")");

    assertThat(result.timedOut()).isFalse();
    assertThat(result.error()).contains("boom");
    assertThat(result.tests()).isEmpty();
  }

  @Test
  void timesOutOnInfiniteLoop() {
    ExecutionResult result = runner.run("javascript", "while (true) {}", "assertEqual(1, 1, \"noop\")");

    assertThat(result.timedOut()).isTrue();
  }

  @Test
  void deniesFileSystemAccess() {
    ExecutionResult result =
        runner.run(
            "javascript",
            "",
            "try { globalThis.__hasFs = typeof require !== 'undefined'; } catch (e) { globalThis.__hasFs = false; }"
                + "assertEqual(typeof require, 'undefined', \"no CommonJS require exposed to guest code\");");

    assertThat(result.error()).isNull();
    assertThat(result.tests()).hasSize(1);
    assertThat(result.tests().get(0).passed()).isTrue();
  }

  @Test
  void rejectsUnsupportedLanguage() {
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> runner.run("python", "print(1)", ""));
  }
}
