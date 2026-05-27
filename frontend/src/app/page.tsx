import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { BarChart3, Link2, Shield, Zap } from "lucide-react";

export default function HomePage() {
  return (
    <div>
      <section className="mx-auto max-w-6xl px-4 py-24 text-center">
        <div className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-accent)] px-4 py-1 text-sm mb-6">
          <Zap className="h-4 w-4 text-[var(--color-primary)]" />
          Production-grade URL shortening
        </div>
        <h1 className="text-4xl font-bold tracking-tight sm:text-6xl bg-gradient-to-r from-[var(--color-primary)] to-purple-400 bg-clip-text text-transparent">
          Shorten links. Track every click.
        </h1>
        <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--color-muted-foreground)]">
          Create branded short URLs, monitor analytics in real time, and manage your links from a beautiful dashboard — like Bitly, built for developers.
        </p>
        <div className="mt-10 flex flex-wrap justify-center gap-4">
          <Link href="/register">
            <Button size="lg">Start for free</Button>
          </Link>
          <Link href="/login">
            <Button size="lg" variant="outline">
              Sign in
            </Button>
          </Link>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 pb-24 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { icon: Link2, title: "Smart shortening", desc: "Custom codes, expiration dates, and instant redirects." },
          { icon: BarChart3, title: "Rich analytics", desc: "Click counts, geo stats, and recent activity per link." },
          { icon: Shield, title: "Secure by default", desc: "JWT auth, role-based access, and admin URL controls." },
          { icon: Zap, title: "Redis-powered", desc: "Cached lookups for lightning-fast redirects." },
        ].map((f) => (
          <Card key={f.title}>
            <CardHeader>
              <f.icon className="h-8 w-8 text-[var(--color-primary)] mb-2" />
              <CardTitle className="text-lg">{f.title}</CardTitle>
              <CardDescription>{f.desc}</CardDescription>
            </CardHeader>
          </Card>
        ))}
      </section>

      <section className="border-t border-[var(--color-border)] bg-[var(--color-muted)]/30 py-16">
        <div className="mx-auto max-w-6xl px-4 text-center">
          <h2 className="text-2xl font-bold mb-4">Ready to shorten your first link?</h2>
          <Link href="/register">
            <Button size="lg">Create your account</Button>
          </Link>
        </div>
      </section>
    </div>
  );
}
