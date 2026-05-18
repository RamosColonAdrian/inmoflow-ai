export type LeadSource =
  | "MANUAL"
  | "EMAIL"
  | "WHATSAPP"
  | "WEB_FORM"
  | "IDEALISTA_EMAIL"
  | "FOTOCASA_EMAIL";

export type LeadStatus =
  | "NEW"
  | "CONTACTED"
  | "WAITING_RESPONSE"
  | "QUALIFIED"
  | "VISIT_SCHEDULED"
  | "NOT_INTERESTED"
  | "LOST"
  | "CONVERTED"
  | "NEEDS_HUMAN";

export type Lead = {
  id: string;
  agencyId: string;
  propertyId: string | null;
  name: string;
  email: string;
  phone: string;
  source: LeadSource;
  status: LeadStatus;
  message: string | null;
  budget: number | string | null;
  desiredZone: string | null;
  score: number | null;
  createdAt: string;
  updatedAt: string;
};

export type PropertyType =
  | "APARTMENT"
  | "HOUSE"
  | "ROOM"
  | "LAND"
  | "COMMERCIAL"
  | "OFFICE"
  | "GARAGE"
  | "STORAGE"
  | "OTHER";

export type OperationType = "SALE" | "RENT";

export type Property = {
  id: string;
  agencyId: string;
  reference: string;
  title: string;
  description: string | null;
  price: number | string | null;
  city: string | null;
  zone: string | null;
  address: string | null;
  rooms: number | null;
  bathrooms: number | null;
  squareMeters: number | null;
  propertyType: PropertyType;
  operationType: OperationType;
  available: boolean;
  sourceUrl: string | null;
  qualificationRulesText: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ConversationChannel =
  | "EMAIL"
  | "WHATSAPP"
  | "PHONE"
  | "WEB_CHAT"
  | "MANUAL";

export type ConversationStatus =
  | "OPEN"
  | "WAITING_LEAD"
  | "WAITING_AGENT"
  | "CLOSED"
  | "NEEDS_HUMAN";

export type Conversation = {
  id: string;
  leadId: string;
  channel: ConversationChannel;
  status: ConversationStatus;
  createdAt: string;
  updatedAt: string;
};

export type SenderType = "LEAD" | "BOT" | "AGENT" | "SYSTEM";

export type Message = {
  id: string;
  conversationId: string;
  senderType: SenderType;
  content: string;
  aiGenerated: boolean;
  sentAt: string;
};

export type AppointmentStatus =
  | "REQUESTED"
  | "PROPOSED"
  | "CONFIRMED"
  | "CANCELLED"
  | "COMPLETED";

export type Appointment = {
  id: string;
  agencyId: string;
  leadId: string;
  propertyId: string;
  conversationId: string;
  requestedDateText: string;
  status: AppointmentStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL;

function buildApiUrl(path: string) {
  if (!API_URL) {
    throw new Error("NEXT_PUBLIC_API_URL is not configured.");
  }

  return `${API_URL.replace(/\/$/, "")}${path}`;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(buildApiUrl(path), {
    ...init,
    headers: {
      Accept: "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}.`);
  }

  return response.json() as Promise<T>;
}

export const apiClient = {
  getLeads(page = 0, size = 20) {
    return request<PageResponse<Lead>>(`/api/leads?page=${page}&size=${size}`);
  },
  getProperties(page = 0, size = 20) {
    return request<PageResponse<Property>>(
      `/api/properties?page=${page}&size=${size}`,
    );
  },
  updatePropertyQualificationRules(
    propertyId: string,
    qualificationRulesText: string,
  ) {
    return request<Property>(
      `/api/properties/${propertyId}/qualification-rules`,
      {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ qualificationRulesText }),
      },
    );
  },
  getConversations(page = 0, size = 20) {
    return request<PageResponse<Conversation>>(
      `/api/conversations?page=${page}&size=${size}`,
    );
  },
  getConversationMessages(conversationId: string) {
    return request<Message[]>(`/api/conversations/${conversationId}/messages`);
  },
  getAppointments(page = 0, size = 20) {
    return request<PageResponse<Appointment>>(
      `/api/appointments?page=${page}&size=${size}`,
    );
  },
  updateAppointmentStatus(
    appointmentId: string,
    status: AppointmentStatus,
  ) {
    return request<Appointment>(`/api/appointments/${appointmentId}/status`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ status }),
    });
  },
  createConversationMessage(
    conversationId: string,
    payload: {
      senderType: Extract<SenderType, "LEAD" | "AGENT">;
      content: string;
      aiGenerated: false;
    },
  ) {
    return request<Message>(`/api/conversations/${conversationId}/messages`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });
  },
};
