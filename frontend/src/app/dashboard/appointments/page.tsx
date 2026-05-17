"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import {
  apiClient,
  type Appointment,
  type Lead,
  type Property,
} from "@/lib/api-client";

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function shortenId(value: string) {
  return value.length > 12 ? `${value.slice(0, 8)}...${value.slice(-4)}` : value;
}

function formatLocation(property: Property | undefined) {
  if (!property) {
    return "-";
  }

  const parts = [property.zone, property.city].filter(Boolean);

  return parts.length > 0 ? parts.join(", ") : "-";
}

function getStatusClass(appointment: Appointment) {
  if (appointment.status === "REQUESTED") {
    return "bg-amber-100 text-amber-900 ring-1 ring-amber-200";
  }

  if (appointment.status === "CONFIRMED") {
    return "bg-emerald-100 text-emerald-800";
  }

  if (appointment.status === "CANCELLED") {
    return "bg-red-100 text-red-800";
  }

  return "bg-slate-100 text-slate-700";
}

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [leads, setLeads] = useState<Lead[]>([]);
  const [properties, setProperties] = useState<Property[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const leadsById = useMemo(() => {
    return new Map(leads.map((lead) => [lead.id, lead]));
  }, [leads]);

  const propertiesById = useMemo(() => {
    return new Map(properties.map((property) => [property.id, property]));
  }, [properties]);

  useEffect(() => {
    let isMounted = true;

    async function loadAppointments() {
      try {
        setIsLoading(true);
        setError(null);

        const [appointmentsResponse, leadsResponse, propertiesResponse] =
          await Promise.all([
            apiClient.getAppointments(0, 20),
            apiClient.getLeads(0, 100),
            apiClient.getProperties(0, 100),
          ]);

        if (isMounted) {
          setAppointments(appointmentsResponse.content);
          setLeads(leadsResponse.content);
          setProperties(propertiesResponse.content);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load appointments.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadAppointments();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <DashboardShell
      eyebrow="Agenda"
      title="Visitas"
      workspaceStatus="Connected to backend API"
    >
      <section className="mx-auto flex w-full max-w-7xl flex-col gap-6">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-slate-950">
            Solicitudes de visita
          </h2>
          <p className="mt-1 max-w-2xl text-sm text-slate-600">
            Appointment requests created from lead conversations, ready for
            review and follow-up.
          </p>
        </div>

        <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
          {isLoading ? (
            <div className="p-6 text-sm text-slate-600">
              Loading appointments...
            </div>
          ) : error ? (
            <div className="border-l-4 border-red-500 bg-red-50 p-6">
              <p className="text-sm font-medium text-red-900">
                Could not load appointments
              </p>
              <p className="mt-1 text-sm text-red-700">{error}</p>
            </div>
          ) : appointments.length === 0 ? (
            <div className="p-6">
              <p className="text-sm font-medium text-slate-950">
                No appointment requests found
              </p>
              <p className="mt-1 text-sm text-slate-600">
                New visit requests from conversations will appear here once
                they are created.
              </p>
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
              {appointments.map((appointment) => {
                const lead = leadsById.get(appointment.leadId);
                const property = propertiesById.get(appointment.propertyId);
                const isPendingRequest = appointment.status === "REQUESTED";

                return (
                  <article
                    key={appointment.id}
                    className={`p-4 transition hover:bg-slate-50 sm:p-5 ${
                      isPendingRequest
                        ? "border-l-4 border-amber-400 bg-amber-50/50"
                        : "border-l-4 border-transparent bg-white"
                    }`}
                  >
                    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="text-base font-semibold text-slate-950">
                            {appointment.requestedDateText}
                          </h3>
                          <span
                            className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${getStatusClass(
                              appointment,
                            )}`}
                          >
                            {formatLabel(appointment.status)}
                          </span>
                          {isPendingRequest ? (
                            <span className="inline-flex rounded-full bg-amber-100 px-2.5 py-1 text-xs font-medium text-amber-900">
                              Pending visit request
                            </span>
                          ) : null}
                        </div>
                        <p className="mt-2 text-sm text-slate-600">
                          Created {formatDate(appointment.createdAt)}
                        </p>
                      </div>

                      <Link
                        href={`/dashboard/conversations?conversationId=${appointment.conversationId}`}
                        className="inline-flex w-fit rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:border-slate-400 hover:bg-white"
                      >
                        Ver conversacion
                      </Link>
                    </div>

                    <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-[1fr_1fr_1.2fr]">
                      <div>
                        <p className="text-xs font-medium uppercase text-slate-500">
                          Lead
                        </p>
                        <p className="mt-1 font-medium text-slate-950">
                          {lead
                            ? lead.name
                            : `Lead no encontrado (${shortenId(
                                appointment.leadId,
                              )})`}
                        </p>
                        <div className="mt-2 space-y-1 text-sm text-slate-600">
                          <p>Telefono: {lead?.phone || "-"}</p>
                          <p>Email: {lead?.email || "-"}</p>
                        </div>
                      </div>

                      <div>
                        <p className="text-xs font-medium uppercase text-slate-500">
                          Inmueble
                        </p>
                        <p className="mt-1 font-medium text-slate-950">
                          {property
                            ? property.title || shortenId(appointment.propertyId)
                            : `Inmueble no encontrado (${shortenId(
                                appointment.propertyId,
                              )})`}
                        </p>
                        <p className="mt-2 text-sm text-slate-600">
                          Zona/Ciudad: {formatLocation(property)}
                        </p>
                      </div>

                      <div className="md:col-span-2 xl:col-span-1">
                        <p className="text-xs font-medium uppercase text-slate-500">
                          Notes
                        </p>
                        <p className="mt-1 whitespace-pre-wrap text-sm leading-6 text-slate-600">
                          {appointment.notes || "-"}
                        </p>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>
      </section>
    </DashboardShell>
  );
}
