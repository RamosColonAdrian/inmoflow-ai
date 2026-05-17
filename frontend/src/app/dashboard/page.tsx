import { DashboardShell } from "@/components/dashboard/DashboardShell";
import { StatCard } from "@/components/dashboard/StatCard";

const stats = [
  {
    label: "Total leads",
    value: "128",
    helperText: "Mock leads captured this month",
  },
  {
    label: "Available properties",
    value: "42",
    helperText: "Mock properties ready to promote",
  },
  {
    label: "Open conversations",
    value: "16",
    helperText: "Mock conversations awaiting follow-up",
  },
  {
    label: "AI responses",
    value: "87",
    helperText: "Mock assisted replies drafted",
  },
];

export default function DashboardPage() {
  return (
    <DashboardShell>
      <section className="mx-auto flex w-full max-w-7xl flex-col gap-6">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-slate-950">
            Agency activity
          </h2>
          <p className="mt-1 max-w-2xl text-sm text-slate-600">
            Mock dashboard metrics for the initial InmoFlow AI frontend layout.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {stats.map((stat) => (
            <StatCard
              key={stat.label}
              label={stat.label}
              value={stat.value}
              helperText={stat.helperText}
            />
          ))}
        </div>

        <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-base font-semibold text-slate-950">
            Today&apos;s focus
          </h3>
          <div className="mt-4 grid gap-4 lg:grid-cols-3">
            <div className="rounded-md bg-slate-50 p-4">
              <p className="text-sm font-medium text-slate-950">
                Review new leads
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Prioritize incoming buyers and renters before assigning agents.
              </p>
            </div>
            <div className="rounded-md bg-slate-50 p-4">
              <p className="text-sm font-medium text-slate-950">
                Update listings
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Keep property availability accurate for future conversations.
              </p>
            </div>
            <div className="rounded-md bg-slate-50 p-4">
              <p className="text-sm font-medium text-slate-950">
                Check AI drafts
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Approve suggested responses before sending them to clients.
              </p>
            </div>
          </div>
        </section>
      </section>
    </DashboardShell>
  );
}
