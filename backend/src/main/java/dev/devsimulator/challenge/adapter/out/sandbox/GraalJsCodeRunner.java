package dev.devsimulator.challenge.adapter.out.sandbox;

import dev.devsimulator.challenge.domain.CodeRunner;
import dev.devsimulator.challenge.domain.ExecutionResult;
import dev.devsimulator.challenge.domain.ExecutionResult.TestResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.springframework.stereotype.Component;

/**
 * Executes untrusted JavaScript in an isolated GraalVM context: no IO, no host access, no thread
 * creation, and a hard wall-clock timeout enforced by force-closing the context from this thread
 * (the standard GraalVM cancellation pattern, since a running eval can't be interrupted directly).
 */
@Component
public class GraalJsCodeRunner implements CodeRunner {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final int MAX_CONSOLE_OUTPUT = 4000;

  private static final String HARNESS_PRELUDE =
      """
      var __results = [];
      function assertEqual(actual, expected, name) {
        var pass = JSON.stringify(actual) === JSON.stringify(expected);
        __results.push({
          name: name,
          passed: pass,
          message: pass ? null : ("expected " + JSON.stringify(expected) + " but got " + JSON.stringify(actual))
        });
      }
      var console = {
        log: function () {
          var parts = [];
          for (var i = 0; i < arguments.length; i++) { parts.push(String(arguments[i])); }
          __log(parts.join(" "));
        }
      };
      """;

  private record Outcome(List<TestResult> tests, String error) {}

  @Override
  public ExecutionResult run(String language, String userCode, String testCode) {
    if (!"javascript".equals(language)) {
      throw new IllegalArgumentException("Unsupported language: " + language);
    }

    StringBuilder consoleOutput = new StringBuilder();
    AtomicReference<Outcome> outcome = new AtomicReference<>();

    Context context =
        Context.newBuilder("js")
            .allowIO(IOAccess.NONE)
            .allowCreateThread(false)
            .allowHostAccess(HostAccess.NONE)
            .build();

    Thread worker =
        new Thread(
            () -> {
              try {
                context
                    .getBindings("js")
                    .putMember(
                        "__log",
                        (ProxyExecutable)
                            args -> {
                              if (consoleOutput.length() < MAX_CONSOLE_OUTPUT && args.length > 0) {
                                consoleOutput.append(args[0].asString()).append('\n');
                              }
                              return null;
                            });
                String script = HARNESS_PRELUDE + "\n" + userCode + "\n" + testCode + "\n__results;";
                Value result = context.eval("js", script);
                outcome.set(new Outcome(toTestResults(result), null));
              } catch (PolyglotException ex) {
                if (!ex.isCancelled()) {
                  outcome.set(new Outcome(List.of(), describe(ex)));
                }
              } catch (Exception ex) {
                outcome.set(new Outcome(List.of(), "Execution failed: " + ex.getMessage()));
              }
            },
            "challenge-code-runner");
    worker.setDaemon(true);
    worker.start();

    joinQuietly(worker, TIMEOUT.toMillis());
    boolean timedOut = worker.isAlive();
    if (timedOut) {
      context.close(true);
      joinQuietly(worker, TIMEOUT.toMillis());
    } else {
      context.close();
    }

    if (timedOut) {
      return new ExecutionResult(
          List.of(), consoleOutput.toString(), "Execution timed out after " + TIMEOUT.getSeconds() + "s", true);
    }

    Outcome result = outcome.get();
    if (result == null) {
      return new ExecutionResult(List.of(), consoleOutput.toString(), "Execution failed", false);
    }
    return new ExecutionResult(result.tests(), consoleOutput.toString(), result.error(), false);
  }

  private String describe(PolyglotException ex) {
    return ex.isGuestException() ? ex.getMessage() : "Execution failed";
  }

  private List<TestResult> toTestResults(Value arrayValue) {
    List<TestResult> results = new ArrayList<>();
    long size = arrayValue.getArraySize();
    for (long i = 0; i < size; i++) {
      Value item = arrayValue.getArrayElement(i);
      String name = item.getMember("name").asString();
      boolean passed = item.getMember("passed").asBoolean();
      Value messageValue = item.getMember("message");
      String message = messageValue.isNull() ? null : messageValue.asString();
      results.add(new TestResult(name, passed, message));
    }
    return results;
  }

  private void joinQuietly(Thread thread, long millis) {
    try {
      thread.join(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
