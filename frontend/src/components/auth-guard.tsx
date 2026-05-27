"use client";

import { useAuthStore } from "@/lib/store";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const router = useRouter();

  useEffect(() => {
    if (!accessToken) {
      router.replace("/login");
    }
  }, [accessToken, router]);

  if (!accessToken) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center text-[var(--color-muted-foreground)]">
        Loading...
      </div>
    );
  }

  return <>{children}</>;
}
