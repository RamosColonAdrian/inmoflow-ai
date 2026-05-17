type HeaderProps = {
  eyebrow?: string;
  title?: string;
  workspaceLabel?: string;
  workspaceStatus?: string;
};

export function Header({
  eyebrow = "Overview",
  title = "Dashboard",
  workspaceLabel = "Demo workspace",
  workspaceStatus = "Backend connected via API URL",
}: HeaderProps) {
  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="flex min-h-16 flex-col justify-center gap-3 px-4 py-4 sm:px-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <div>
          <p className="text-sm font-medium text-slate-500">{eyebrow}</p>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-950">
            {title}
          </h1>
        </div>
        <div className="flex items-center gap-3">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-medium text-slate-950">
              {workspaceLabel}
            </p>
            <p className="text-xs text-slate-500">{workspaceStatus}</p>
          </div>
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-sm font-semibold text-emerald-800">
            IF
          </div>
        </div>
      </div>
    </header>
  );
}
