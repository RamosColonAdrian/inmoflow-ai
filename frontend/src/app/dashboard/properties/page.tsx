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
];

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatPrice(value: number | string) {
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
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </DashboardShell>
  );
}
