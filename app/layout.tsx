import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DevSimulator — Practice by building",
  description: "Build real-world skills one bug at a time."
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
