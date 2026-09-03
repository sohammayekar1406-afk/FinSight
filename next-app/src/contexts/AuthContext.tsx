/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useState, useCallback, useEffect } from "react"
import {
  storeCredentials,
  getStoredCredentials,
  clearCredentials,
  encodeBasicAuth,
} from "@/api/client"
import { apiClient } from "@/api/client"
import type { UserRole } from "@/types/api"

interface AuthUser {
  username: string
  role: UserRole
  encodedAuth: string
}

interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

// Determine role from username — matches backend SecurityConfig
function inferRole(username: string): UserRole {
  if (username === "admin") return "ADMIN"
  if (username === "analyst") return "ANALYST"
  return "OPERATOR"
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = getStoredCredentials()
    if (stored) {
      return {
        username: stored.username,
        role: inferRole(stored.username),
        encodedAuth: encodeBasicAuth(stored.username, stored.password),
      }
    }
    return null
  })

  const isLoading = false

  // Listen for 401 events from the axios interceptor
  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null)
    }
    window.addEventListener("finsight:unauthorized", handleUnauthorized)
    return () => window.removeEventListener("finsight:unauthorized", handleUnauthorized)
  }, [])

  const login = useCallback(async (username: string, password: string) => {
    // Validate by calling the dashboard stats endpoint with credentials
    const encoded = encodeBasicAuth(username, password)
    const { data } = await apiClient.get("/api/dashboard/stats", {
      headers: { Authorization: `Basic ${encoded}` },
    })
    // If we reach here, credentials are valid
    if (data) {
      const role = inferRole(username)
      storeCredentials({ username, password, role })
      setUser({ username, role, encodedAuth: encoded })
    }
  }, [])

  const logout = useCallback(() => {
    clearCredentials()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error("useAuth must be used within AuthProvider")
  return ctx
}
