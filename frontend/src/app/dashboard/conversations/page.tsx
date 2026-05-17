"use client";

import { useEffect, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import { apiClient, type Conversation } from "@/lib/api-client";

const columns = ["ID", "Lead ID", "Channel", "Status", "Created", "Updated"];

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

export default function ConversationsPage() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadConversations() {
      try {
        setIsLoading(true);
        setError(null);

        const response = await apiClient.getConversations(0, 20);

        if (isMounted) {
          setConversations(response.content);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load conversations.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadConversations();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <DashboardShell
      eyebrow="Inbox"
      title="Conversations"
      workspaceStatus="Connected to backend API"
    >
      <section className="mx-auto flex w-full max-w-7xl flex-col gap-6">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-slate-950">
            Conversation inbox
          </h2>
          <p className="mt-1 max-w-2xl text-sm text-slate-600">
            Active and historical lead conversations from the backend.
          </p>
        </div>

        <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
          {isLoading ? (
            <div className="p-6 text-sm text-slate-600">
              Loading conversations...
            </div>
          ) : error ? (
            <div className="border-l-4 border-red-500 bg-red-50 p-6">
              <p className="text-sm font-medium text-red-900">
                Could not load conversations
              </p>
              <p className="mt-1 text-sm text-red-700">{error}</p>
            </div>
          ) : conversations.length === 0 ? (
            <div className="p-6">
              <p className="text-sm font-medium text-slate-950">
                No conversations found
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Backend conversations will appear here once they are created.
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
                  {conversations.map((conversation) => (
                    <tr key={conversation.id} className="hover:bg-slate-50">
                      <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-950">
                        {conversation.id}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {conversation.leadId}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatLabel(conversation.channel)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3">
                        <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700">
                          {formatLabel(conversation.status)}
                        </span>
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatDate(conversation.createdAt)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatDate(conversation.updatedAt)}
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
