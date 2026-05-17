type StatCardProps = {
  label: string;
  value: string;
  helperText: string;
};

export function StatCard({ label, value, helperText }: StatCardProps) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className="mt-3 text-3xl font-semibold tracking-tight text-slate-950">
        {value}
      </p>
      <p className="mt-2 text-sm text-slate-500">{helperText}</p>
    </article>
  );
}
