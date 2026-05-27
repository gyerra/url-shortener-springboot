import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "./store";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const api = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = useAuthStore.getState().refreshToken;
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API_URL}/api/auth/refresh`, {
            refreshToken,
          });
          useAuthStore.getState().setAuth(
            data.accessToken,
            data.refreshToken,
            data.user
          );
          original.headers.Authorization = `Bearer ${data.accessToken}`;
          return api(original);
        } catch {
          useAuthStore.getState().clearAuth();
        }
      }
    }
    return Promise.reject(error);
  }
);

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: { id: number; username: string; email: string; role: string };
}

export interface UrlItem {
  id: number;
  originalUrl: string;
  shortCode: string;
  shortUrl: string;
  clickCount: number;
  title?: string;
  expiresAt?: string;
  disabled: boolean;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DashboardStats {
  totalLinks: number;
  totalClicks: number;
  mostPopularUrl?: UrlItem;
  recentUrls: UrlItem[];
  topPerformingLinks: UrlItem[];
}

export interface UrlAnalytics {
  urlId: number;
  shortCode: string;
  originalUrl: string;
  clickCount: number;
  recentClicks: { ipAddress: string; userAgent: string; country: string; clickedAt: string }[];
  topCountries: { country: string; count: number }[];
}

export const authApi = {
  register: (data: { username: string; email: string; password: string }) =>
    api.post<AuthResponse>("/api/auth/register", data),
  login: (data: { usernameOrEmail: string; password: string }) =>
    api.post<AuthResponse>("/api/auth/login", data),
};

export const urlApi = {
  create: (data: { originalUrl: string; title?: string; expiresAt?: string; customShortCode?: string }) =>
    api.post<UrlItem>("/api/urls/create", data),
  my: (params?: { search?: string; page?: number; size?: number; sortBy?: string; direction?: string }) =>
    api.get<PageResponse<UrlItem>>("/api/urls/my", { params }),
  dashboard: () => api.get<DashboardStats>("/api/urls/dashboard"),
  update: (id: number, data: { originalUrl?: string; title?: string; expiresAt?: string }) =>
    api.put<UrlItem>(`/api/urls/${id}`, data),
  delete: (id: number) => api.delete(`/api/urls/${id}`),
  analytics: (id: number) => api.get<UrlAnalytics>(`/api/urls/analytics/${id}`),
};
