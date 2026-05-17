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
  price: number | string;
  city: string;
  zone: string;
  address: string | null;
  rooms: number | null;
  bathrooms: number | null;
  squareMeters: number | null;
  propertyType: PropertyType;
  operationType: OperationType;
  available: boolean;
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

async function request<T>(path: string): Promise<T> {
  const response = await fetch(buildApiUrl(path), {
    headers: {
      Accept: "application/json",
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
};
