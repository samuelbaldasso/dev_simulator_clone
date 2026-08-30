"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

type Challenge = {
  id: number;
  title: string;
  difficulty: string;
  xp: number;
  description: string;
};

type ChallengePage = {
  content: Challenge[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export default function ChallengesPage() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");

  useEffect(() => {
    const controller = new AbortController();

    async function loadChallenges() {
      try {
        const response = await fetch(`${API_URL}/api/challenges?size=20`, { signal: controller.signal });
        if (!response.ok) throw new Error("Could not load challenges");
        const challengePage: ChallengePage = await response.json();
        setChallenges(challengePage.content);
        setStatus("ready");
      } catch (error) {
        if ((error as DOMException).name !== "AbortError") setStatus("error");
      }
    }

    loadChallenges();
    return () => controller.abort();
  }, []);

  return <main>
    <nav className="nav wrap">
      <Link className="brand" href="/" aria-label="DevSimulator home"><i>&lt;/&gt;</i> devsimulator</Link>
      <div className="navlinks open">
        <Link href="/">Home</Link>
      </div>
    </nav>

    <section className="section wrap">
      <div className="section-kicker">YOUR NEXT TASK</div>
      <h2>Pick a <em>challenge.</em></h2>

      {status === "loading" && <p className="lead">Loading challenges...</p>}
      {status === "error" && <p className="lead">The API is unavailable. Start the Spring application to load challenges.</p>}
      {status === "ready" && challenges.length === 0 && <p className="lead">No challenges available yet.</p>}

      <div className="feature-grid">
        {challenges.map((challenge) => (
          <Link className="feature" href={`/challenges/${challenge.id}`} key={challenge.id} style={{ display: "block", textDecoration: "none", color: "inherit" }}>
            <div className="icon purple">↗</div>
            <h3>{challenge.title}</h3>
            <p>{challenge.description}</p>
            <div className="pipeline">
              <small>DIFFICULTY</small>
              <strong>{challenge.difficulty} <span>+{challenge.xp} XP</span></strong>
            </div>
          </Link>
        ))}
      </div>
    </section>
  </main>;
}
