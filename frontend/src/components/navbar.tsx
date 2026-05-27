"use client";

import Link from "next/link";
import { useAuthStore } from "@/lib/store";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";
import { Link2, LogOut } from "lucide-react";
import { useRouter } from "next/navigation";

export function Navbar() {
  const { user, clearAuth, accessToken } = useAuthStore();
  const router = useRouter();

  const logout = () => {
    clearAuth();
    router.push("/login");
  };

  return (
    <header className="sticky top-0 z-50 border-b border-[var(--color-border)] bg-[var(--color-background)]/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
        <Link href="/" className="flex items-center gap-2 font-bold text-lg">
          <Link2 className="h-6 w-6 text-[var(--color-primary)]" />
          ShortLink
        </Link>
        <nav className="flex items-center gap-2">
          <ThemeToggle />
          {accessToken ? (
            <>
              <Link href="/dashboard">
                <Button variant="ghost">Dashboard</Button>
              </Link>
              <span className="hidden text-sm text-[var(--color-muted-foreground)] sm:inline">
                {user?.username}
              </span>
              <Button variant="ghost" size="icon" onClick={logout} aria-label="Logout">
                <LogOut className="h-4 w-4" />
              </Button>
            </>
          ) : (
            <>
              <Link href="/login">
                <Button variant="ghost">Login</Button>
              </Link>
              <Link href="/register">
                <Button>Get Started</Button>
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
