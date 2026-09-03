/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate, Outlet, useLocation } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import AppLayout from "@/layouts/AppLayout"
import PublicLayout from "@/layouts/PublicLayout"
import LandingPage from "@/pages/LandingPage"
import LoginPage from "@/pages/LoginPage"
import DashboardPage from "@/pages/DashboardPage"
import ExceptionsPage from "@/pages/ExceptionsPage"
import ExceptionDetailPage from "@/pages/ExceptionDetailPage"
import InvestigationsPage from "@/pages/InvestigationsPage"
import InvestigationDetailPage from "@/pages/InvestigationDetailPage"
import TransactionsPage from "@/pages/TransactionsPage"
import ReconciliationPage from "@/pages/ReconciliationPage"
import AuditLogsPage from "@/pages/AuditLogsPage"
import SettingsPage from "@/pages/SettingsPage"

// Protected route guard
function RequireAuth() {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="w-6 h-6 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}

// Public-only guard (redirects authed users to dashboard)
function PublicOnly() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="w-6 h-6 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export const router = createBrowserRouter([
  // Public routes
  {
    element: <PublicOnly />,
    children: [
      {
        element: <PublicLayout />,
        children: [
          { path: "/", element: <LandingPage /> },
          { path: "/login", element: <LoginPage /> },
        ],
      },
    ],
  },
  // Protected routes
  {
    element: <RequireAuth />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/dashboard", element: <DashboardPage /> },
          { path: "/exceptions", element: <ExceptionsPage /> },
          { path: "/exceptions/:exceptionId", element: <ExceptionDetailPage /> },
          { path: "/investigations", element: <InvestigationsPage /> },
          { path: "/investigations/:exceptionId", element: <InvestigationDetailPage /> },
          { path: "/transactions", element: <TransactionsPage /> },
          { path: "/reconciliation", element: <ReconciliationPage /> },
          { path: "/audit-logs", element: <AuditLogsPage /> },
          { path: "/settings", element: <SettingsPage /> },
        ],
      },
    ],
  },
  // Fallback
  { path: "*", element: <Navigate to="/" replace /> },
])
