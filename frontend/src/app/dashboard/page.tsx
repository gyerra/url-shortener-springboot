"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useState } from "react";
import { AuthGuard } from "@/components/auth-guard";
import { CopyButton } from "@/components/copy-button";
import { QrDialog } from "@/components/qr-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { urlApi } from "@/lib/api";
import { BarChart3, Link2, MousePointerClick, Search, Trash2, TrendingUp } from "lucide-react";
import { toast } from "sonner";

export default function DashboardPage() {
  return (
    <AuthGuard>
      <DashboardContent />
    </AuthGuard>
  );
}

function DashboardContent() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [newUrl, setNewUrl] = useState({ originalUrl: "", title: "", customShortCode: "" });

  const { data: stats } = useQuery({
    queryKey: ["dashboard"],
    queryFn: async () => (await urlApi.dashboard()).data,
  });

  const { data: urls, isLoading } = useQuery({
    queryKey: ["urls", search, page],
    queryFn: async () =>
      (await urlApi.my({ search: search || undefined, page, size: 10, sortBy: "createdAt", direction: "DESC" }))
        .data,
  });

  const createMutation = useMutation({
    mutationFn: () =>
      urlApi.create({
        originalUrl: newUrl.originalUrl,
        title: newUrl.title || undefined,
        customShortCode: newUrl.customShortCode || undefined,
      }),
    onSuccess: () => {
      toast.success("URL shortened!");
      setNewUrl({ originalUrl: "", title: "", customShortCode: "" });
      queryClient.invalidateQueries({ queryKey: ["urls"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
    onError: () => toast.error("Failed to create URL"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => urlApi.delete(id),
    onSuccess: () => {
      toast.success("Link deleted");
      queryClient.invalidateQueries({ queryKey: ["urls"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
  });

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 space-y-8">
      <div>
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <p className="text-[var(--color-muted-foreground)]">Manage and track your shortened links</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon={Link2} label="Total links" value={stats?.totalLinks ?? 0} />
        <StatCard icon={MousePointerClick} label="Total clicks" value={stats?.totalClicks ?? 0} />
        <StatCard
          icon={TrendingUp}
          label="Top link clicks"
          value={stats?.mostPopularUrl?.clickCount ?? 0}
        />
        <StatCard icon={BarChart3} label="Recent links" value={stats?.recentUrls?.length ?? 0} />
      </div>

      {stats?.mostPopularUrl && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Most popular URL</CardTitle>
            <CardDescription>{stats.mostPopularUrl.originalUrl}</CardDescription>
          </CardHeader>
          <CardContent className="flex items-center gap-2">
            <code className="text-sm text-[var(--color-primary)]">{stats.mostPopularUrl.shortUrl}</code>
            <CopyButton text={stats.mostPopularUrl.shortUrl} />
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Shorten a new URL</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createMutation.mutate();
            }}
            className="grid gap-4 sm:grid-cols-2"
          >
            <div className="space-y-2 sm:col-span-2">
              <Label>Destination URL</Label>
              <Input
                placeholder="https://example.com/long-page"
                value={newUrl.originalUrl}
                onChange={(e) => setNewUrl({ ...newUrl, originalUrl: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label>Title (optional)</Label>
              <Input
                value={newUrl.title}
                onChange={(e) => setNewUrl({ ...newUrl, title: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Custom code (optional)</Label>
              <Input
                value={newUrl.customShortCode}
                onChange={(e) => setNewUrl({ ...newUrl, customShortCode: e.target.value })}
                maxLength={12}
              />
            </div>
            <Button type="submit" disabled={createMutation.isPending} className="sm:col-span-2 w-fit">
              {createMutation.isPending ? "Creating..." : "Shorten URL"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
          <Input
            className="pl-10"
            placeholder="Search links..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Your links</CardTitle>
          <CardDescription>
            {urls ? `${urls.totalElements} total` : "Loading..."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading && <p className="text-[var(--color-muted-foreground)]">Loading...</p>}
          {urls?.content.map((url) => (
            <div
              key={url.id}
              className="flex flex-col gap-3 rounded-lg border border-[var(--color-border)] p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="min-w-0 flex-1">
                <p className="font-medium truncate">{url.title || url.originalUrl}</p>
                <p className="text-sm text-[var(--color-muted-foreground)] truncate">{url.originalUrl}</p>
                <p className="text-sm text-[var(--color-primary)] mt-1">{url.shortUrl}</p>
                <p className="text-xs text-[var(--color-muted-foreground)] mt-1">
                  {url.clickCount} clicks · {new Date(url.createdAt).toLocaleDateString()}
                </p>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <CopyButton text={url.shortUrl} />
                <QrDialog url={url.shortUrl} />
                <Link href={`/dashboard/analytics/${url.id}`}>
                  <Button variant="outline" size="sm">
                    Analytics
                  </Button>
                </Link>
                <Button
                  variant="destructive"
                  size="icon"
                  onClick={() => deleteMutation.mutate(url.id)}
                  aria-label="Delete"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
          {urls && urls.totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <Button variant="outline" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                Previous
              </Button>
              <span className="flex items-center text-sm">
                Page {page + 1} of {urls.totalPages}
              </span>
              <Button
                variant="outline"
                disabled={urls.last}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function StatCard({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: number;
}) {
  return (
    <Card>
      <CardContent className="pt-6">
        <div className="flex items-center gap-3">
          <div className="rounded-lg bg-[var(--color-accent)] p-2">
            <Icon className="h-5 w-5 text-[var(--color-primary)]" />
          </div>
          <div>
            <p className="text-2xl font-bold">{value.toLocaleString()}</p>
            <p className="text-sm text-[var(--color-muted-foreground)]">{label}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
