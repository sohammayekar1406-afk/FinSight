import axios from "axios"

// In development the Vite proxy forwards /api/* to http://localhost:8080,
// so we use an empty baseURL (same-origin). In production set VITE_API_BASE_URL
// to the real backend URL (e.g. https://api.yourdomain.com).
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ""

// Axios instance — all API calls go through this
export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000,
})

// Auth token storage key
const AUTH_KEY = "finsight_auth"

export interface StoredCredentials {
  username: string
  password: string
  role: string
}

export function storeCredentials(creds: StoredCredentials) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(creds))
}

export function getStoredCredentials(): StoredCredentials | null {
  try {
    const raw = localStorage.getItem(AUTH_KEY)
    if (!raw) return null
    return JSON.parse(raw) as StoredCredentials
  } catch {
    return null
  }
}

export function clearCredentials() {
  localStorage.removeItem(AUTH_KEY)
}

export function encodeBasicAuth(username: string, password: string): string {
  return btoa(`${username}:${password}`)
}

// Request interceptor — attach Basic Auth header on every request
apiClient.interceptors.request.use((config) => {
  const creds = getStoredCredentials()
  if (creds) {
    config.headers["Authorization"] = `Basic ${encodeBasicAuth(creds.username, creds.password)}`
  }
  return config
})

// Response interceptor — handle 401 globally
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearCredentials()
      // Emit custom event so AuthContext can react
      window.dispatchEvent(new CustomEvent("finsight:unauthorized"))
    }
    return Promise.reject(error)
  }
)
