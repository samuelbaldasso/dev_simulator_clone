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

const Arrow = () => <span aria-hidden="true">→</span>;

export default function Home() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [started, setStarted] = useState(false);
  const [challenge, setChallenge] = useState<Challenge | null>(null);
  const [challengeStatus, setChallengeStatus] = useState<"loading" | "ready" | "error">("loading");

  useEffect(() => {
    const controller = new AbortController();

    async function loadChallenge() {
      try {
        const response = await fetch(`${API_URL}/api/challenges`, { signal: controller.signal });
        if (!response.ok) throw new Error("Could not load challenges");
        const challengePage: ChallengePage = await response.json();
        setChallenge(challengePage.content[0] ?? null);
        setChallengeStatus("ready");
      } catch (error) {
        if ((error as DOMException).name !== "AbortError") setChallengeStatus("error");
      }
    }

    loadChallenge();
    return () => controller.abort();
  }, []);

  return <main>
    <nav className="nav wrap">
      <a className="brand" href="#top" aria-label="DevSimulator home"><i>&lt;/&gt;</i> devsimulator</a>
      <button className="menu" onClick={() => setMenuOpen(!menuOpen)} aria-expanded={menuOpen} aria-label="Toggle menu">☰</button>
      <div className={`navlinks ${menuOpen ? "open" : ""}`}>
        <a href="#how">How it works</a><Link href="/challenges">Challenges</Link>
        <Link className="small-cta" href="/challenges">Explore challenges <Arrow /></Link>
      </div>
    </nav>

    <section id="top" className="hero wrap">
      <div className="eyebrow"><b>✦</b> THE PRACTICE PLATFORM FOR DEVELOPERS</div>
      <h1>Get better at coding.<br /><em>One real bug at a time.</em></h1>
      <p className="lead">Build real-world skills by fixing realistic bugs in production-style projects. Earn XP, level up, and grow at your own pace.</p>
      <div className="hero-actions">
        <Link className="primary" href="/challenges" onClick={() => setStarted(true)}>Explore challenges <Arrow /></Link>
        <a href="#how" className="watch"><span>▶</span> See how it works</a>
      </div>
      {started && <p className="welcome" role="status">You&apos;re ready to start. Your challenges are ready below.</p>}
      <div className="social-proof"><div className="avatars"><span>J</span><span>M</span><span>K</span><span>A</span></div><span><b>Join 2,000+ developers</b><br />building sharper skills</span></div>
      <div className="orb orb-one" /><div className="orb orb-two" />
    </section>

    <section id="how" className="section wrap">
      <div className="section-kicker">WHY DEVS CHOOSE DEVSIMULATOR</div>
      <h2>Practice that feels<br />like the <em>real thing.</em></h2>
      <div id="challenges" className="feature-grid">
        <article className="feature"><div className="icon coral">⌘</div><h3>Real-world tasks</h3><p>No abstract puzzles. Debug the kinds of issues you&apos;ll actually meet on the job.</p><div className="feed-card"><div><b>amanda</b><small>2m</small><span className="photo sunset" /></div><p>Sunset vibes at the beach 🌅</p><small>♡ 42 likes</small><div><b>michael</b><small>12m</small><span className="photo city" /></div><p>City lights never get old 🌃</p></div></article>
        <article className="feature"><div className="icon purple">↗</div><h3>Gamified progression</h3><p>Earn XP, level up, and unlock new projects as your skills grow.</p><div className="pipeline"><small>REVIEW PIPELINE</small><strong>↥ <span>{challengeStatus === "loading" ? "Loading challenge..." : "Pushing code..."}</span></strong><div className="progress"><i /></div><div className="steps"><b>✓</b><b>✓</b><b>✓</b><b className="active">4</b></div><p>{challenge?.difficulty ?? "Your next task"} <em>{challenge ? `+${challenge.xp} XP` : ""}</em></p></div></article>
        <article className="feature"><div className="icon mint">◐</div><h3>Learn by doing</h3><p>{challengeStatus === "error" ? "The API is unavailable. Start the Spring application to load the current task." : challenge?.description ?? "Loading a real-world task from the API..."}</p><div className="editor"><header><span>{challenge?.title ?? "Loading challenge..."}</span><i>● ● ●</i></header><pre><code><i>1</i> public decimal <b>CalculateTotal</b>() {'\n'}<i>2</i>   var subtotal = GetSubtotal();{'\n'}<i>3</i>   var tax = subtotal * <em>0.08m</em>;{'\n'}<i>4</i>   return subtotal;{'\n'}<i>5</i> {'}'}</code></pre><footer>JavaScript UTF-8 · LF <b>Java</b></footer></div></article>
      </div>
    </section>

    <section className="levels"><div className="wrap levels-content"><div><div className="section-kicker">YOUR JOURNEY</div><h2>Grow at your<br /><em>own pace.</em></h2><p>Start simple. Tackle harder challenges as you gain confidence. No pressure, no deadlines.</p></div><div className="level-card"><div className="level-top"><span>LEVEL 03</span><b>✦ 46 / 300 XP</b></div><div className="level-bar"><i /></div><div className="level-path"><div className="done">✓<small>Student</small></div><div className="current">⌁<small>Junior</small></div><div>◒<small>Mid-level</small></div><div>♔<small>Senior</small></div></div></div></div></section>

    <footer><div className="wrap footer-inner"><a className="brand" href="#top"><i>&lt;/&gt;</i> devsimulator</a><span>© 2025 DevSimulator. Built for developers.</span></div></footer>
  </main>;
}
