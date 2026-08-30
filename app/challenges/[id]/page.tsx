"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

const Editor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

type ChallengeDetail = {
  id: number;
  title: string;
  difficulty: string;
  xp: number;
  description: string;
  runnable: boolean;
  language: string | null;
  starterCode: string | null;
};

type TestResult = {
  name: string;
  passed: boolean;
  message: string | null;
};

type RunResult = {
  tests: TestResult[];
  consoleOutput: string;
  error: string | null;
  timedOut: boolean;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export default function ChallengeDetailPage() {
  const params = useParams<{ id: string }>();
  const [challenge, setChallenge] = useState<ChallengeDetail | null>(null);
  const [status, setStatus] = useState<"loading" | "ready" | "notfound" | "error">("loading");
  const [code, setCode] = useState("");
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState<RunResult | null>(null);
  const [runError, setRunError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadChallenge() {
      try {
        const response = await fetch(`${API_URL}/api/challenges/${params.id}`, { signal: controller.signal });
        if (response.status === 404) {
          setStatus("notfound");
          return;
        }
        if (!response.ok) throw new Error("Could not load challenge");
        const data: ChallengeDetail = await response.json();
        setChallenge(data);
        setCode(data.starterCode ?? "");
        setStatus("ready");
      } catch (error) {
        if ((error as DOMException).name !== "AbortError") setStatus("error");
      }
    }

    loadChallenge();
    return () => controller.abort();
  }, [params.id]);

  async function runCode() {
    if (!challenge) return;
    setRunning(true);
    setRunError(null);
    setResult(null);
    try {
      const response = await fetch(`${API_URL}/api/challenges/${challenge.id}/run`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      });
      if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(body?.message ?? "Could not run code");
      }
      setResult(await response.json());
    } catch (error) {
      setRunError((error as Error).message);
    } finally {
      setRunning(false);
    }
  }

  return <main>
    <nav className="nav wrap">
      <Link className="brand" href="/" aria-label="DevSimulator home"><i>&lt;/&gt;</i> devsimulator</Link>
      <div className="navlinks open">
        <Link href="/challenges">← All challenges</Link>
      </div>
    </nav>

    <section className="section wrap">
      {status === "loading" && <p className="lead">Loading challenge...</p>}
      {status === "notfound" && <p className="lead">Challenge not found.</p>}
      {status === "error" && <p className="lead">The API is unavailable. Start the Spring application to load this challenge.</p>}

      {status === "ready" && challenge && (
        <>
          <div className="section-kicker">{challenge.difficulty} · +{challenge.xp} XP</div>
          <h2>{challenge.title}</h2>
          <p className="lead" style={{ margin: "0 0 30px" }}>{challenge.description}</p>

          {!challenge.runnable && (
            <div className="feature">
              <p>This is a design-level challenge with no runnable code — think through your approach and discuss it with your team or mentor.</p>
            </div>
          )}

          {challenge.runnable && (
            <div style={{ display: "grid", gridTemplateColumns: "1.3fr 1fr", gap: 24, alignItems: "start" }}>
              <div className="editor">
                <header style={{ height: "auto", alignItems: "center", padding: "10px 14px" }}>
                  <span>solution.js</span>
                  <button
                    className="small-cta"
                    onClick={runCode}
                    disabled={running}
                    style={{ padding: "6px 14px", fontSize: 12, borderRadius: 6 }}
                  >
                    {running ? "Running..." : "Run tests"}
                  </button>
                </header>
                <Editor
                  height="420px"
                  language="javascript"
                  theme="vs-dark"
                  value={code}
                  onChange={(value) => setCode(value ?? "")}
                  options={{ minimap: { enabled: false }, fontSize: 13 }}
                />
              </div>

              <div className="pipeline" style={{ minHeight: 420 }}>
                <small>TEST RESULTS</small>
                {runError && <p style={{ color: "#ff8d78" }}>{runError}</p>}
                {!result && !runError && <p>Run your code to see results here.</p>}
                {result?.timedOut && <p style={{ color: "#ff8d78" }}>Execution timed out — check for infinite loops.</p>}
                {result?.error && <p style={{ color: "#ff8d78" }}>{result.error}</p>}
                {result && result.tests.length > 0 && (
                  <ul style={{ listStyle: "none", padding: 0, margin: "12px 0" }}>
                    {result.tests.map((test) => (
                      <li key={test.name} style={{ marginBottom: 8, color: test.passed ? "#9ce4bc" : "#ff8d78" }}>
                        {test.passed ? "✓" : "✗"} {test.name}
                        {!test.passed && test.message && <div style={{ color: "#b5c8bb", fontSize: 10 }}>{test.message}</div>}
                      </li>
                    ))}
                  </ul>
                )}
                {result?.consoleOutput && (
                  <>
                    <p style={{ borderTop: "1px solid #405048", paddingTop: 12, marginBottom: 4 }}>console output</p>
                    <pre style={{ whiteSpace: "pre-wrap", margin: 0 }}>{result.consoleOutput}</pre>
                  </>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </section>
  </main>;
}
