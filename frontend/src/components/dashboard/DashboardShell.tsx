import type { ReactNode } from "react";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";

type DashboardShellProps = {
  children: ReactNode;
  eyebrow?: string;
  title?: string;
  workspaceLabel?: string;
  workspaceStatus?: string;
};

export function DashboardShell({
  children,
  eyebrow,
  title,
  workspaceLabel,
  workspaceStatus,
}: DashboardShellProps) {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <div className="flex min-h-screen flex-col lg:flex-row">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <Header
            eyebrow={eyebrow}
            title={title}
            workspaceLabel={workspaceLabel}
            workspaceStatus={workspaceStatus}
          />
          <main className="min-w-0 max-w-full flex-1 px-4 py-6 sm:px-6 lg:px-8">
            {children}
          </main>
        </div>
      </div>
    </div>
  );
}
