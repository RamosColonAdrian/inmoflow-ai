"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import {
  apiClient,
  type Appointment,
  type Conversation,
  type Lead,
} from "@/lib/api-client";

type PipelineColumnId =
  | "new"
  | "contacted"
  | "qualified"
  | "visitRequested"
  | "visitConfirmed"
  | "needsHuman"
  | "lost"
  | "converted";

type PipelineColumn = {
  id: PipelineColumnId;
  title: string;
  surfaceClassName: string;
  headerClassName: string;
  accentClassName: string;
  badgeClassName: string;
};

const pipelineColumns: PipelineColumn[] = [
  {
    id: "new",
    title: "Nuevo",
    surfaceClassName: "bg-slate-50/80",
    headerClassName: "bg-sky-50/70",
    accentClassName: "border-t-sky-300",
    badgeClassName: "bg-sky-100 text-sky-800 ring-sky-200",
  },
  {
    id: "contacted",
    title: "Contactado",
    surfaceClassName: "bg-indigo-50/50",
    headerClassName: "bg-indigo-50/80",
    accentClassName: "border-t-indigo-300",
    badgeClassName: "bg-indigo-100 text-indigo-800 ring-indigo-200",
  },
  {
    id: "qualified",
    title: "Cualificado",
    surfaceClassName: "bg-green-50/50",
    headerClassName: "bg-green-50/80",
    accentClassName: "border-t-green-300",
    badgeClassName: "bg-green-100 text-green-800 ring-green-200",
  },
  {
    id: "visitRequested",
    title: "Visita solicitada",
    surfaceClassName: "bg-amber-50/50",
    headerClassName: "bg-amber-50/80",
    accentClassName: "border-t-amber-300",
    badgeClassName: "bg-amber-100 text-amber-800 ring-amber-200",
  },
  {
    id: "visitConfirmed",
    title: "Visita confirmada",
    surfaceClassName: "bg-emerald-50/50",
    headerClassName: "bg-emerald-50/80",
    accentClassName: "border-t-emerald-300",
    badgeClassName: "bg-emerald-100 text-emerald-800 ring-emerald-200",
  },
  {
    id: "needsHuman",
    title: "Requiere agente",
    surfaceClassName: "bg-orange-50/50",
    headerClassName: "bg-orange-50/80",
    accentClassName: "border-t-orange-300",
    badgeClassName: "bg-orange-100 text-orange-800 ring-orange-200",
  },
  {
    id: "lost",
    title: "Perdido",
    surfaceClassName: "bg-red-50/50",
    headerClassName: "bg-red-50/80",
    accentClassName: "border-t-red-300",
    badgeClassName: "bg-red-100 text-red-800 ring-red-200",
  },
  {
    id: "converted",
    title: "Convertido",
    surfaceClassName: "bg-purple-50/50",
    headerClassName: "bg-purple-50/80",
    accentClassName: "border-t-purple-300",
    badgeClassName: "bg-purple-100 text-purple-800 ring-purple-200",
  },
];

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatBudget(value: Lead["budget"]) {
  if (value === null || value === "") {
    return "-";
  }

  const amount = typeof value === "string" ? Number(value) : value;

  if (!Number.isFinite(amount)) {
    return String(value);
  }

  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 0,
  }).format(amount);
}

function getPipelineColumnId(
  lead: Lead,
  requestedAppointment: Appointment | undefined,
): PipelineColumnId {
  if (requestedAppointment) {
    return "visitRequested";
  }

  switch (lead.status) {
    case "NEW":
      return "new";
    case "CONTACTED":
    case "WAITING_RESPONSE":
      return "contacted";
    case "QUALIFIED":
      return "qualified";
    case "VISIT_SCHEDULED":
      return "visitConfirmed";
    case "NEEDS_HUMAN":
      return "needsHuman";
    case "NOT_INTERESTED":
    case "LOST":
      return "lost";
    case "CONVERTED":
      return "converted";
  }
}

export default function PipelinePage() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const conversationsByLeadId = useMemo(() => {
    return new Map(
      conversations.map((conversation) => [conversation.leadId, conversation]),
    );
  }, [conversations]);

  const requestedAppointmentsByLeadId = useMemo(() => {
    const appointmentsByLeadId = new Map<string, Appointment>();

    appointments.forEach((appointment) => {
      if (
        appointment.status === "REQUESTED" &&
        !appointmentsByLeadId.has(appointment.leadId)
      ) {
        appointmentsByLeadId.set(appointment.leadId, appointment);
      }
    });

    return appointmentsByLeadId;
  }, [appointments]);

  const leadsByColumn = useMemo(() => {
    const groupedLeads = new Map<PipelineColumnId, Lead[]>(
      pipelineColumns.map((column) => [column.id, []]),
    );

    leads.forEach((lead) => {
      const columnId = getPipelineColumnId(
        lead,
        requestedAppointmentsByLeadId.get(lead.id),
      );

      groupedLeads.get(columnId)?.push(lead);
    });

    return groupedLeads;
  }, [leads, requestedAppointmentsByLeadId]);

  useEffect(() => {
    let isMounted = true;

    async function loadPipeline() {
      try {
        setIsLoading(true);
        setError(null);

        const [leadsResponse, conversationsResponse, appointmentsResponse] =
          await Promise.all([
            apiClient.getLeads(0, 100),
            apiClient.getConversations(0, 100),
            apiClient.getAppointments(0, 100),
          ]);

        if (isMounted) {
          setLeads(leadsResponse.content);
          setConversations(conversationsResponse.content);
          setAppointments(appointmentsResponse.content);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load lead pipeline.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadPipeline();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <DashboardShell
      eyebrow="CRM"
      title="Pipeline"
      workspaceStatus="Connected to backend API"
    >
      <section className="flex w-full max-w-full min-w-0 flex-col gap-6">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-slate-950">
            Pipeline comercial de leads
          </h2>
          <p className="mt-1 max-w-2xl text-sm text-slate-600">
            Vista por estado comercial para revisar oportunidades, visitas
            solicitadas y casos que requieren seguimiento manual.
          </p>
        </div>

        {isLoading ? (
          <section className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-600 shadow-sm">
            Loading pipeline...
          </section>
        ) : error ? (
          <section className="border-l-4 border-red-500 bg-red-50 p-6">
            <p className="text-sm font-medium text-red-900">
              Could not load pipeline
            </p>
            <p className="mt-1 text-sm text-red-700">{error}</p>
          </section>
        ) : (
          <section className="w-full max-w-full min-w-0 overflow-x-auto overscroll-x-contain pb-6">
            <div className="flex w-max min-w-max flex-nowrap gap-4 pr-4">
              {pipelineColumns.map((column) => {
                const columnLeads = leadsByColumn.get(column.id) ?? [];

                return (
                  <section
                    key={column.id}
                    className={`flex min-h-[32rem] w-[300px] min-w-[300px] shrink-0 flex-col rounded-lg border border-t-4 border-slate-200 shadow-sm sm:w-[320px] sm:min-w-[320px] ${column.accentClassName} ${column.surfaceClassName}`}
                  >
                    <header
                      className={`border-b border-slate-200 px-4 py-3 ${column.headerClassName}`}
                    >
                      <div className="flex items-center justify-between gap-3">
                        <h3 className="text-sm font-semibold text-slate-950">
                          {column.title}
                        </h3>
                        <span
                          className={`rounded-full px-2 py-1 text-xs font-semibold ring-1 ${column.badgeClassName}`}
                        >
                          {columnLeads.length}
                        </span>
                      </div>
                    </header>

                    <div className="flex flex-1 flex-col gap-3 p-3">
                      {columnLeads.length === 0 ? (
                        <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-center">
                          <p className="text-sm font-medium text-slate-950">
                            Sin leads
                          </p>
                          <p className="mt-1 text-xs text-slate-600">
                            No hay oportunidades en esta etapa.
                          </p>
                        </div>
                      ) : (
                        columnLeads.map((lead) => {
                          const conversation = conversationsByLeadId.get(
                            lead.id,
                          );
                          const requestedAppointment =
                            requestedAppointmentsByLeadId.get(lead.id);
                          const contact = lead.phone || lead.email || "-";

                          return (
                            <article
                              key={lead.id}
                              className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-slate-300 hover:shadow"
                            >
                              <div className="flex items-start justify-between gap-3">
                                <div className="min-w-0 flex-1">
                                  <h4 className="break-words text-sm font-semibold leading-5 text-slate-950">
                                    {lead.name}
                                  </h4>
                                  <p className="mt-1 break-words text-[11px] leading-4 text-slate-600">
                                    {contact}
                                  </p>
                                </div>
                                <span className="shrink-0 rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700 ring-1 ring-slate-200">
                                  {lead.score ?? "-"}
                                </span>
                              </div>

                              <dl className="mt-4 space-y-2.5 text-xs">
                                <div>
                                  <dt className="text-[10px] font-semibold uppercase text-slate-500">
                                    Origen
                                  </dt>
                                  <dd className="mt-0.5 break-words text-slate-700">
                                    {formatLabel(lead.source)}
                                  </dd>
                                </div>
                                <div>
                                  <dt className="text-[10px] font-semibold uppercase text-slate-500">
                                    Zona
                                  </dt>
                                  <dd className="mt-0.5 break-words text-slate-700">
                                    {lead.desiredZone || "-"}
                                  </dd>
                                </div>
                                <div>
                                  <dt className="text-[10px] font-semibold uppercase text-slate-500">
                                    Presupuesto
                                  </dt>
                                  <dd className="mt-0.5 break-words text-slate-700">
                                    {formatBudget(lead.budget)}
                                  </dd>
                                </div>
                                {requestedAppointment ? (
                                  <div>
                                    <dt className="text-[10px] font-semibold uppercase text-slate-500">
                                      Visita solicitada
                                    </dt>
                                    <dd className="mt-0.5 break-words text-slate-700">
                                      {requestedAppointment.requestedDateText}
                                    </dd>
                                  </div>
                                ) : null}
                              </dl>

                              {conversation ? (
                                <Link
                                  href={`/dashboard/conversations?conversationId=${conversation.id}`}
                                  className="mt-4 inline-flex w-full justify-center rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                >
                                  Ver conversacion
                                </Link>
                              ) : null}
                            </article>
                          );
                        })
                      )}
                    </div>
                  </section>
                );
              })}
            </div>
          </section>
        )}
      </section>
    </DashboardShell>
  );
}
