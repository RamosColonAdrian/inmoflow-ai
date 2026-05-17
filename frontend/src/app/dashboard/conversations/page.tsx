"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { DashboardShell } from "@/components/dashboard/DashboardShell";
import {
  apiClient,
  type Conversation,
  type Message,
  type SenderType,
} from "@/lib/api-client";

type SendableSenderType = Extract<SenderType, "LEAD" | "AGENT">;

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

function formatTime(value: string) {
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function getMessageContainerClass(senderType: SenderType) {
  if (senderType === "LEAD") {
    return "items-start";
  }

  if (senderType === "SYSTEM") {
    return "items-center";
  }

  return "items-end";
}

function getMessageBubbleClass(senderType: SenderType) {
  switch (senderType) {
    case "LEAD":
      return "border-slate-200 bg-white text-slate-900";
    case "BOT":
      return "border-emerald-200 bg-emerald-50 text-emerald-950";
    case "AGENT":
      return "border-blue-200 bg-blue-50 text-blue-950";
    case "SYSTEM":
      return "border-slate-200 bg-slate-100 text-slate-600";
  }
}

export default function ConversationsPage() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<
    string | null
  >(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoadingConversations, setIsLoadingConversations] = useState(true);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [conversationsError, setConversationsError] = useState<string | null>(
    null,
  );
  const [messagesError, setMessagesError] = useState<string | null>(null);
  const [sendError, setSendError] = useState<string | null>(null);
  const [senderType, setSenderType] = useState<SendableSenderType>("AGENT");
  const [content, setContent] = useState("");

  const selectedConversation = useMemo(
    () =>
      conversations.find(
        (conversation) => conversation.id === selectedConversationId,
      ) ?? null,
    [conversations, selectedConversationId],
  );

  const sortedMessages = useMemo(
    () =>
      [...messages].sort(
        (first, second) =>
          new Date(first.sentAt).getTime() - new Date(second.sentAt).getTime(),
      ),
    [messages],
  );

  useEffect(() => {
    let isMounted = true;

    async function loadConversations() {
      try {
        setIsLoadingConversations(true);
        setConversationsError(null);

        const response = await apiClient.getConversations(0, 20);

        if (isMounted) {
          setConversations(response.content);
          setSelectedConversationId(response.content[0]?.id ?? null);
        }
      } catch (loadError) {
        if (isMounted) {
          setConversationsError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load conversations.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingConversations(false);
        }
      }
    }

    loadConversations();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!selectedConversationId) {
      setMessages([]);
      return;
    }

    const conversationId = selectedConversationId;
    let isMounted = true;

    async function loadMessages() {
      try {
        setIsLoadingMessages(true);
        setMessagesError(null);
        setSendError(null);

        const response =
          await apiClient.getConversationMessages(conversationId);

        if (isMounted) {
          setMessages(response);
        }
      } catch (loadError) {
        if (isMounted) {
          setMessagesError(
            loadError instanceof Error
              ? loadError.message
              : "Unable to load messages.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoadingMessages(false);
        }
      }
    }

    loadMessages();

    return () => {
      isMounted = false;
    };
  }, [selectedConversationId]);

  async function reloadMessages(conversationId: string) {
    const response = await apiClient.getConversationMessages(conversationId);
    setMessages(response);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedConversationId || !content.trim()) {
      return;
    }

    try {
      setIsSending(true);
      setSendError(null);

      await apiClient.createConversationMessage(selectedConversationId, {
        senderType,
        content: content.trim(),
        aiGenerated: false,
      });

      setContent("");
      await reloadMessages(selectedConversationId);
    } catch (submitError) {
      setSendError(
        submitError instanceof Error
          ? submitError.message
          : "Unable to send message.",
      );
    } finally {
      setIsSending(false);
    }
  }

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
            Select a lead conversation to review the complete message history
            and send a follow-up.
          </p>
        </div>

        <section className="grid min-h-[34rem] overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm lg:grid-cols-[22rem_1fr]">
          <aside className="border-b border-slate-200 bg-white lg:border-b-0 lg:border-r">
            <div className="border-b border-slate-200 bg-slate-50 px-4 py-3">
              <h3 className="text-sm font-semibold text-slate-950">
                Conversations
              </h3>
              <p className="mt-1 text-xs text-slate-600">
                Latest 20 conversations from the backend.
              </p>
            </div>

            {isLoadingConversations ? (
              <div className="p-4 text-sm text-slate-600">
                Loading conversations...
              </div>
            ) : conversationsError ? (
              <div className="border-l-4 border-red-500 bg-red-50 p-4">
                <p className="text-sm font-medium text-red-900">
                  Could not load conversations
                </p>
                <p className="mt-1 text-sm text-red-700">
                  {conversationsError}
                </p>
              </div>
            ) : conversations.length === 0 ? (
              <div className="p-4">
                <p className="text-sm font-medium text-slate-950">
                  No conversations found
                </p>
                <p className="mt-1 text-sm text-slate-600">
                  Backend conversations will appear here once they are created.
                </p>
              </div>
            ) : (
              <div className="max-h-[42rem] divide-y divide-slate-100 overflow-y-auto">
                {conversations.map((conversation) => {
                  const isSelected = conversation.id === selectedConversationId;

                  return (
                    <button
                      key={conversation.id}
                      type="button"
                      onClick={() => setSelectedConversationId(conversation.id)}
                      className={`block w-full px-4 py-3 text-left transition hover:bg-slate-50 ${
                        isSelected
                          ? "border-l-4 border-slate-950 bg-slate-50"
                          : "border-l-4 border-transparent"
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-slate-950">
                            Lead {conversation.leadId}
                          </p>
                          <p className="mt-1 text-xs text-slate-600">
                            {formatLabel(conversation.channel)}
                          </p>
                        </div>
                        <span className="shrink-0 rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">
                          {formatLabel(conversation.status)}
                        </span>
                      </div>
                      <p className="mt-2 text-xs text-slate-500">
                        Updated {formatDate(conversation.updatedAt)}
                      </p>
                    </button>
                  );
                })}
              </div>
            )}
          </aside>

          <div className="flex min-h-[34rem] flex-col bg-slate-50">
            <div className="border-b border-slate-200 bg-white px-4 py-3">
              {selectedConversation ? (
                <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <h3 className="text-sm font-semibold text-slate-950">
                      Conversation {selectedConversation.id}
                    </h3>
                    <p className="mt-1 text-xs text-slate-600">
                      Lead {selectedConversation.leadId} -{" "}
                      {formatLabel(selectedConversation.channel)}
                    </p>
                  </div>
                  <span className="w-fit rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700">
                    {formatLabel(selectedConversation.status)}
                  </span>
                </div>
              ) : (
                <div>
                  <h3 className="text-sm font-semibold text-slate-950">
                    Message history
                  </h3>
                  <p className="mt-1 text-xs text-slate-600">
                    Select a conversation to view its messages.
                  </p>
                </div>
              )}
            </div>

            <div className="flex-1 overflow-y-auto p-4">
              {!selectedConversationId ? (
                <div className="rounded-lg border border-dashed border-slate-300 bg-white p-6 text-center">
                  <p className="text-sm font-medium text-slate-950">
                    No conversation selected
                  </p>
                  <p className="mt-1 text-sm text-slate-600">
                    Choose a conversation from the list to see the full history.
                  </p>
                </div>
              ) : isLoadingMessages ? (
                <div className="text-sm text-slate-600">
                  Loading messages...
                </div>
              ) : messagesError ? (
                <div className="border-l-4 border-red-500 bg-red-50 p-4">
                  <p className="text-sm font-medium text-red-900">
                    Could not load messages
                  </p>
                  <p className="mt-1 text-sm text-red-700">{messagesError}</p>
                </div>
              ) : sortedMessages.length === 0 ? (
                <div className="rounded-lg border border-dashed border-slate-300 bg-white p-6 text-center">
                  <p className="text-sm font-medium text-slate-950">
                    No messages yet
                  </p>
                  <p className="mt-1 text-sm text-slate-600">
                    Send the first message using the form below.
                  </p>
                </div>
              ) : (
                <div className="flex flex-col gap-3">
                  {sortedMessages.map((message) => (
                    <article
                      key={message.id}
                      className={`flex flex-col ${getMessageContainerClass(
                        message.senderType,
                      )}`}
                    >
                      <div
                        className={`max-w-[min(42rem,85%)] rounded-lg border px-4 py-3 shadow-sm ${getMessageBubbleClass(
                          message.senderType,
                        )}`}
                      >
                        <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-xs font-medium">
                          <span>{formatLabel(message.senderType)}</span>
                          <span className="text-slate-400">-</span>
                          <time dateTime={message.sentAt}>
                            {formatTime(message.sentAt)}
                          </time>
                        </div>
                        <p className="mt-2 whitespace-pre-wrap text-sm leading-6">
                          {message.content}
                        </p>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </div>

            <form
              onSubmit={handleSubmit}
              className="border-t border-slate-200 bg-white p-4"
            >
              <div className="flex flex-col gap-3">
                <label
                  htmlFor="message-content"
                  className="text-sm font-medium text-slate-950"
                >
                  New message
                </label>
                <textarea
                  id="message-content"
                  value={content}
                  onChange={(event) => setContent(event.target.value)}
                  rows={3}
                  disabled={!selectedConversationId || isSending}
                  placeholder="Write a message..."
                  className="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 disabled:cursor-not-allowed disabled:bg-slate-100"
                />
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <select
                    value={senderType}
                    onChange={(event) =>
                      setSenderType(event.target.value as SendableSenderType)
                    }
                    disabled={!selectedConversationId || isSending}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 shadow-sm outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200 disabled:cursor-not-allowed disabled:bg-slate-100 sm:w-40"
                  >
                    <option value="AGENT">AGENT</option>
                    <option value="LEAD">LEAD</option>
                  </select>
                  <button
                    type="submit"
                    disabled={
                      !selectedConversationId || !content.trim() || isSending
                    }
                    className="rounded-lg bg-slate-950 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300"
                  >
                    {isSending ? "Sending..." : "Send message"}
                  </button>
                </div>
                {sendError ? (
                  <p className="text-sm text-red-700">{sendError}</p>
                ) : null}
              </div>
            </form>
          </div>
        </section>
      </section>
    </DashboardShell>
  );
}
