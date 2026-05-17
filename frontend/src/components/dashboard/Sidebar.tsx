import Link from "next/link";

const navigationItems = [
  "Dashboard",
  "Agencies",
  "Properties",
  "Leads",
  "Conversations",
  "AI Assistant",
  "Settings",
];

export function Sidebar() {
  return (
    <aside className="border-b border-slate-200 bg-white lg:min-h-screen lg:w-64 lg:border-b-0 lg:border-r">
      <div className="flex h-full flex-col gap-6 px-4 py-5">
        <Link href="/dashboard" className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-950 text-sm font-semibold text-white">
            IF
          </div>
          <div>
            <p className="text-sm font-semibold text-slate-950">InmoFlow AI</p>
            <p className="text-xs text-slate-500">Real estate SaaS</p>
          </div>
        </Link>

        <nav aria-label="Dashboard navigation" className="flex-1">
          <ul className="flex gap-2 overflow-x-auto pb-1 lg:flex-col lg:overflow-visible lg:pb-0">
            {navigationItems.map((item) => {
              const isActive = item === "Dashboard";

              return (
                <li key={item} className="shrink-0 lg:shrink">
                  <Link
                    href={item === "Dashboard" ? "/dashboard" : "#"}
                    className={`block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                      isActive
                        ? "bg-slate-950 text-white"
                        : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
                    }`}
                  >
                    {item}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </div>
    </aside>
  );
}
