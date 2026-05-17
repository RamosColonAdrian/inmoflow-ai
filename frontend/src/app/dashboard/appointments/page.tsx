"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import { apiClient, type Appointment } from "@/lib/api-client";

const columns = [
  "Fecha solicitada",
  "Status",
  "Lead",
  "Property",
  "Conversation",
  "Notes",
  "Created",
  "Action",
];

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
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadAppointments() {
      try {
        setIsLoading(true);
        setError(null);

        const response = await apiClient.getAppointments(0, 20);

        if (isMounted) {
          setAppointments(response.content);
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
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    {columns.map((column) => (
                      <th
                        key={column}
                        scope="col"
                        className="whitespace-nowrap px-4 py-3 font-medium text-slate-600"
                      >
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {appointments.map((appointment) => {
                    const isPendingRequest =
                      appointment.status === "REQUESTED";

                    return (
                      <tr
                        key={appointment.id}
                        className={`hover:bg-slate-50 ${
                          isPendingRequest ? "bg-amber-50/50" : ""
                        }`}
                      >
                        <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-950">
                          {appointment.requestedDateText}
                          {isPendingRequest ? (
                            <p className="mt-1 text-xs font-medium text-amber-800">
                              Pending visit request
                            </p>
                          ) : null}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span
                            className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${getStatusClass(
                              appointment,
                            )}`}
                          >
                            {formatLabel(appointment.status)}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                          {appointment.leadId}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                          {appointment.propertyId}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                          {appointment.conversationId}
                        </td>
                        <td className="max-w-xs px-4 py-3 text-slate-600">
                          <span className="line-clamp-2">
                            {appointment.notes || "-"}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                          {formatDate(appointment.createdAt)}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <Link
                            href={`/dashboard/conversations?conversationId=${appointment.conversationId}`}
                            className="inline-flex rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                          >
                            Ver conversacion
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </DashboardShell>
  );
}
