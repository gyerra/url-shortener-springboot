"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useParams } from "next/navigation";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { AuthGuard } from "@/components/auth-guard";
import { CopyButton } from "@/components/copy-button";
import { QrDialog } from "@/components/qr-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { urlApi } from "@/lib/api";
import { ArrowLeft, Globe, MousePointerClick } from "lucide-react";

export default function AnalyticsPage() {
  return (
    <AuthGuard>
      <AnalyticsContent />
    </AuthGuard>
  );
}

function AnalyticsContent() {
  const params = useParams();
  const id = Number(params.id);

  const { data, isLoading, error } = useQuery({
    queryKey: ["analytics", id],
    queryFn: async () => (await urlApi.analytics(id)).data,
    enabled: !Number.isNaN(id),
  });

  if (isLoading) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-12 text-center text-[var(--color-muted-foreground)]">
        Loading analytics...
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-12 text-center">
        <p>Failed to load analytics</p>
        <Link href="/dashboard">
          <Button variant="outline" className="mt-4">
            Back to dashboard
          </Button>
        </Link>
      </div>
    );
  }

  const chartData = data.topCountries.map((c) => ({
    country: c.country,
    clicks: c.count,
  }));

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 space-y-8">
      <div className="flex items-center gap-4">
        <Link href="/dashboard">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Link analytics</h1>
          <p className="text-sm text-[var(--color-muted-foreground)] truncate max-w-xl">
            {data.originalUrl}
          </p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <code className="rounded-lg bg-[var(--color-muted)] px-3 py-1 text-sm">
          /{data.shortCode}
        </code>
        <CopyButton
          text={`${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"}/${data.shortCode}`}
        />
        <QrDialog
          url={`${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"}/${data.shortCode}`}
        />
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <CardContent className="pt-6 flex items-center gap-3">
            <MousePointerClick className="h-8 w-8 text-[var(--color-primary)]" />
            <div>
              <p className="text-3xl font-bold">{data.clickCount}</p>
              <p className="text-sm text-[var(--color-muted-foreground)]">Total clicks</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6 flex items-center gap-3">
            <Globe className="h-8 w-8 text-[var(--color-primary)]" />
            <div>
              <p className="text-3xl font-bold">{data.topCountries.length}</p>
              <p className="text-sm text-[var(--color-muted-foreground)]">Countries tracked</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Clicks by country</CardTitle>
          <CardDescription>Geographic distribution of clicks</CardDescription>
        </CardHeader>
        <CardContent className="h-72">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                <XAxis dataKey="country" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="clicks" fill="var(--color-primary)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-center text-[var(--color-muted-foreground)] py-12">
              No click data yet
            </p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Recent clicks</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[var(--color-border)] text-left">
                  <th className="pb-2 pr-4">Time</th>
                  <th className="pb-2 pr-4">Country</th>
                  <th className="pb-2 pr-4">IP</th>
                  <th className="pb-2">User agent</th>
                </tr>
              </thead>
              <tbody>
                {data.recentClicks.map((click, i) => (
                  <tr key={i} className="border-b border-[var(--color-border)]/50">
                    <td className="py-2 pr-4 whitespace-nowrap">
                      {new Date(click.clickedAt).toLocaleString()}
                    </td>
                    <td className="py-2 pr-4">{click.country || "—"}</td>
                    <td className="py-2 pr-4">{click.ipAddress || "—"}</td>
                    <td className="py-2 max-w-xs truncate text-[var(--color-muted-foreground)]">
                      {click.userAgent || "—"}
                    </td>
                  </tr>
                ))}
                {data.recentClicks.length === 0 && (
                  <tr>
                    <td colSpan={4} className="py-8 text-center text-[var(--color-muted-foreground)]">
                      No clicks recorded yet
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
