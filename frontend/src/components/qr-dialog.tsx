"use client";

import { QRCodeSVG } from "qrcode.react";
import * as Dialog from "@radix-ui/react-dialog";
import { Button } from "@/components/ui/button";
import { QrCode } from "lucide-react";

export function QrDialog({ url }: { url: string }) {
  return (
    <Dialog.Root>
      <Dialog.Trigger asChild>
        <Button variant="outline" size="icon" aria-label="QR code">
          <QrCode className="h-4 w-4" />
        </Button>
      </Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50" />
        <Dialog.Content className="fixed left-1/2 top-1/2 z-50 w-full max-w-sm -translate-x-1/2 -translate-y-1/2 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6 shadow-xl">
          <Dialog.Title className="text-lg font-semibold mb-4">QR Code</Dialog.Title>
          <div className="flex justify-center rounded-lg bg-white p-4">
            <QRCodeSVG value={url} size={200} />
          </div>
          <p className="mt-4 text-center text-sm text-[var(--color-muted-foreground)] break-all">
            {url}
          </p>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
