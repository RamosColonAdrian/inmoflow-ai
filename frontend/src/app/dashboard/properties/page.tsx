"use client";

import { useEffect, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import { apiClient, type Property } from "@/lib/api-client";

const columns = [
  "Reference",
  "Title",
  "City",
  "Zone",
  "Price",
  "Rooms",
  "Bathrooms",
  "Property type",
  "Operation",
  "Available",
  "Rules",
  "Actions",
];

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatPrice(value: number | string | null) {
  if (value === null) {
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

export default function PropertiesPage() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingProperty, setEditingProperty] = useState<Property | null>(null);
  const [rulesDraft, setRulesDraft] = useState("");
  const [isSavingRules, setIsSavingRules] = useState(false);
  const [rulesError, setRulesError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadProperties() {
      try {
        setIsLoading(true);
        setError(null);

        const response = await apiClient.getProperties(0, 20);

        if (isMounted) {
          setProperties(response.content);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load properties.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadProperties();

    return () => {
      isMounted = false;
    };
  }, []);

  function openRulesEditor(property: Property) {
    setEditingProperty(property);
    setRulesDraft(property.qualificationRulesText ?? "");
    setRulesError(null);
  }

  function closeRulesEditor() {
    if (isSavingRules) {
      return;
    }

    setEditingProperty(null);
    setRulesDraft("");
    setRulesError(null);
  }

  async function saveRules() {
    if (!editingProperty) {
      return;
    }

    try {
      setIsSavingRules(true);
      setRulesError(null);

      const updatedProperty = await apiClient.updatePropertyQualificationRules(
        editingProperty.id,
        rulesDraft,
      );

      setProperties((currentProperties) =>
        currentProperties.map((property) =>
          property.id === updatedProperty.id ? updatedProperty : property,
        ),
      );
      setEditingProperty(null);
      setRulesDraft("");
    } catch (saveError) {
      setRulesError(
        saveError instanceof Error
          ? saveError.message
          : "Unable to save qualification rules.",
      );
    } finally {
      setIsSavingRules(false);
    }
  }

  return (
    <DashboardShell
      eyebrow="Portfolio"
      title="Properties"
      workspaceStatus="Connected to backend API"
    >
      <section className="mx-auto flex w-full max-w-7xl flex-col gap-6">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-slate-950">
            Property inventory
          </h2>
          <p className="mt-1 max-w-2xl text-sm text-slate-600">
            Listings from the backend, including availability and commercial
            details for each property.
          </p>
        </div>

        <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
          {isLoading ? (
            <div className="p-6 text-sm text-slate-600">
              Loading properties...
            </div>
          ) : error ? (
            <div className="border-l-4 border-red-500 bg-red-50 p-6">
              <p className="text-sm font-medium text-red-900">
                Could not load properties
              </p>
              <p className="mt-1 text-sm text-red-700">{error}</p>
            </div>
          ) : properties.length === 0 ? (
            <div className="p-6">
              <p className="text-sm font-medium text-slate-950">
                No properties found
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Backend properties will appear here once they are created.
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
                  {properties.map((property) => (
                    <tr key={property.id} className="hover:bg-slate-50">
                      <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-950">
                        {property.reference}
                      </td>
                      <td className="max-w-xs px-4 py-3 font-medium text-slate-950">
                        <span className="line-clamp-2">{property.title}</span>
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {property.city}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {property.zone}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatPrice(property.price)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {property.rooms ?? "-"}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {property.bathrooms ?? "-"}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatLabel(property.propertyType)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatLabel(property.operationType)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                            property.available
                              ? "bg-emerald-100 text-emerald-800"
                              : "bg-slate-100 text-slate-700"
                          }`}
                        >
                          {property.available ? "Available" : "Unavailable"}
                        </span>
                      </td>
                      <td className="whitespace-nowrap px-4 py-3">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                            property.qualificationRulesText?.trim()
                              ? "bg-sky-100 text-sky-800"
                              : "bg-slate-100 text-slate-700"
                          }`}
                        >
                          {property.qualificationRulesText?.trim()
                            ? "Reglas añadidas"
                            : "Sin reglas"}
                        </span>
                      </td>
                      <td className="whitespace-nowrap px-4 py-3">
                        <button
                          type="button"
                          onClick={() => openRulesEditor(property)}
                          className="rounded-md border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                        >
                          Editar reglas
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>

      {editingProperty ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6">
          <div className="w-full max-w-2xl rounded-lg bg-white shadow-xl">
            <div className="border-b border-slate-200 px-6 py-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Reglas internas
              </p>
              <h3 className="mt-1 text-lg font-semibold text-slate-950">
                {editingProperty.title}
              </h3>
            </div>

            <div className="px-6 py-5">
              <label
                htmlFor="qualification-rules"
                className="text-sm font-medium text-slate-700"
              >
                Reglas de cualificación
              </label>
              <textarea
                id="qualification-rules"
                value={rulesDraft}
                onChange={(event) => setRulesDraft(event.target.value)}
                rows={8}
                className="mt-2 w-full resize-y rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                placeholder="No mascotas. No estudiantes. Solo larga estancia. Se pide contrato laboral y nóminas."
              />

              {editingProperty.qualificationRulesText?.trim() ? (
                <div className="mt-4 rounded-md bg-slate-50 p-3">
                  <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                    Reglas actuales
                  </p>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-slate-700">
                    {editingProperty.qualificationRulesText}
                  </p>
                </div>
              ) : null}

              {rulesError ? (
                <p className="mt-3 text-sm text-red-700">{rulesError}</p>
              ) : null}
            </div>

            <div className="flex justify-end gap-3 border-t border-slate-200 px-6 py-4">
              <button
                type="button"
                onClick={closeRulesEditor}
                disabled={isSavingRules}
                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={saveRules}
                disabled={isSavingRules}
                className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isSavingRules ? "Guardando..." : "Guardar"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </DashboardShell>
  );
}
