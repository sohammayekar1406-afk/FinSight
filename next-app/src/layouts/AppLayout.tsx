import { useState, useEffect, useCallback } from "react"
import { Outlet, useNavigate, useLocation } from "react-router-dom"
import { motion, AnimatePresence } from "motion/react"
import { useAuth } from "@/contexts/AuthContext"
import { useTheme } from "@/components/theme-provider"
import { useDashboardStats } from "@/hooks/useDashboard"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  DropdownMenuGroup,
} from "@/components/ui/dropdown-menu"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Badge } from "@/components/ui/badge"
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from "@/components/ui/command"
import {
  LayoutDashboard,
  AlertTriangle,
  Search as SearchIcon,
  Activity,
  ArrowLeftRight,
  GitBranch,
  ScrollText,
  Settings,
  LogOut,
  Sun,
  Moon,
  Monitor,
  Menu,
  ChevronLeft,
  ChevronRight,
  Bell,
  User,
  Zap,
} from "lucide-react"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"

// Nav items
const NAV_ITEMS = [
  { path: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { path: "/exceptions", icon: AlertTriangle, label: "Exceptions" },
  { path: "/investigations", icon: SearchIcon, label: "Investigations" },
  { path: "/transactions", icon: ArrowLeftRight, label: "Transactions" },
  { path: "/reconciliation", icon: GitBranch, label: "Reconciliation" },
  { path: "/audit-logs", icon: ScrollText, label: "Audit Logs" },
]

const BOTTOM_NAV = [{ path: "/settings", icon: Settings, label: "Settings" }]

// Breadcrumb label map
const BREADCRUMB_MAP: Record<string, string> = {
  dashboard: "Dashboard",
  exceptions: "Exceptions",
  investigations: "Investigations",
  transactions: "Transactions",
  reconciliation: "Reconciliation",
  "audit-logs": "Audit Logs",
  settings: "Settings",
}

function useBreadcrumbs() {
  const location = useLocation()
  const parts = location.pathname.split("/").filter(Boolean)
  return parts.map((part, i) => ({
    label: BREADCRUMB_MAP[part] ?? part,
    path: "/" + parts.slice(0, i + 1).join("/"),
    isLast: i === parts.length - 1,
  }))
}

// Sidebar nav item
function NavItem({
  item,
  collapsed,
  active,
  onClick,
}: {
  item: (typeof NAV_ITEMS)[0]
  collapsed: boolean
  active: boolean
  onClick?: () => void
}) {
  const navigate = useNavigate()

  const content = (
    <button
      onClick={() => {
        navigate(item.path)
        onClick?.()
      }}
      className={`
        w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium
        transition-all duration-150 group relative
        ${
          active
            ? "bg-muted text-foreground"
            : "text-muted-foreground hover:text-foreground hover:bg-muted/60"
        }
        ${collapsed ? "justify-center px-2" : ""}
      `}
    >
      {active && (
        <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-foreground/40 rounded-r-full" />
      )}
      <item.icon className={`w-4 h-4 flex-shrink-0`} />
      {!collapsed && <span>{item.label}</span>}
    </button>
  )

  if (collapsed) {
    return (
      <Tooltip>
        <TooltipTrigger>{content}</TooltipTrigger>
        <TooltipContent side="right" className="text-xs">
          {item.label}
        </TooltipContent>
      </Tooltip>
    )
  }

  return content
}

// Desktop sidebar
function DesktopSidebar({ collapsed, onToggle }: { collapsed: boolean; onToggle: () => void }) {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <aside
      className={`
        hidden lg:flex flex-col h-screen border-r border-border bg-sidebar
        transition-all duration-300 ease-out flex-shrink-0
        ${collapsed ? "w-[60px]" : "w-[220px]"}
      `}
    >
      {/* Logo */}
      <div
        className={`flex items-center h-14 px-3 border-b border-border ${collapsed ? "justify-center px-4" : "gap-2 px-4"}`}
      >
        {!collapsed && (
          <button
            onClick={() => navigate("/")}
            className="text-base font-normal tracking-tight text-foreground hover:opacity-80 transition-opacity cursor-pointer"
            style={{ fontFamily: "'Fraunces', serif" }}
            aria-label="Go to landing page"
          >
            FinSight
          </button>
        )}
        {collapsed && (
          <button
            onClick={() => navigate("/")}
            className="text-xs font-semibold text-foreground hover:opacity-80 transition-opacity cursor-pointer"
            style={{ fontFamily: "'Fraunces', serif" }}
            aria-label="Go to landing page"
          >
            FS
          </button>
        )}
      </div>

      {/* Main nav */}
      <nav className="flex-1 p-2 flex flex-col gap-0.5 overflow-y-auto">
        <TooltipProvider delay={0}>
          {NAV_ITEMS.map((item) => (
            <NavItem
              key={item.path}
              item={item}
              collapsed={collapsed}
              active={location.pathname.startsWith(item.path)}
            />
          ))}
        </TooltipProvider>
      </nav>

      <Separator />

      {/* Bottom nav */}
      <nav className="p-2 flex flex-col gap-0.5">
        <TooltipProvider delay={0}>
          {BOTTOM_NAV.map((item) => (
            <NavItem
              key={item.path}
              item={item}
              collapsed={collapsed}
              active={location.pathname.startsWith(item.path)}
            />
          ))}
        </TooltipProvider>
      </nav>

      {/* Collapse toggle */}
      <div className="p-3 border-t border-border">
        <button
          onClick={onToggle}
          className={`w-full flex items-center justify-center rounded-md p-1.5 text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors ${collapsed ? "" : "gap-2 justify-end"}`}
          aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? (
            <ChevronRight className="w-4 h-4" />
          ) : (
            <>
              <span className="text-xs">Collapse</span>
              <ChevronLeft className="w-4 h-4" />
            </>
          )}
        </button>
      </div>
    </aside>
  )
}

// Mobile sidebar
function MobileSidebar() {
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger
        render={
          <Button variant="ghost" size="icon" className="lg:hidden w-9 h-9" aria-label="Open menu">
            <Menu className="w-5 h-5" />
          </Button>
        }
      />
      <SheetContent side="left" className="w-[240px] p-0 bg-sidebar border-r border-border">
        {/* Logo */}
        <div className="flex items-center gap-2 h-14 px-4 border-b border-border">
          <button
            onClick={() => navigate("/")}
            className="text-base font-normal tracking-tight text-foreground hover:opacity-80 transition-opacity cursor-pointer"
            style={{ fontFamily: "'Fraunces', serif" }}
            aria-label="Go to landing page"
          >
            FinSight
          </button>
        </div>

        <nav className="flex-1 p-2 flex flex-col gap-0.5">
          {NAV_ITEMS.map((item) => (
            <NavItem
              key={item.path}
              item={item}
              collapsed={false}
              active={location.pathname.startsWith(item.path)}
              onClick={() => setOpen(false)}
            />
          ))}
        </nav>
      </SheetContent>
    </Sheet>
  )
}

// Command palette
function CommandCenter({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (v: boolean) => void
}) {
  const navigate = useNavigate()

  const run = (path: string) => {
    navigate(path)
    onOpenChange(false)
  }

  return (
    <CommandDialog open={open} onOpenChange={onOpenChange}>
      <CommandInput placeholder="Search commands, pages, exceptions..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>
        <CommandGroup heading="Navigation">
          <CommandItem onSelect={() => run("/dashboard")}>
            <LayoutDashboard className="mr-2 w-4 h-4" />
            Open Dashboard
          </CommandItem>
          <CommandItem onSelect={() => run("/exceptions")}>
            <AlertTriangle className="mr-2 w-4 h-4" />
            View Exceptions
          </CommandItem>
          <CommandItem onSelect={() => run("/investigations")}>
            <SearchIcon className="mr-2 w-4 h-4" />
            View Investigations
          </CommandItem>
          <CommandItem onSelect={() => run("/transactions")}>
            <ArrowLeftRight className="mr-2 w-4 h-4" />
            View Transactions
          </CommandItem>
          <CommandItem onSelect={() => run("/audit-logs")}>
            <ScrollText className="mr-2 w-4 h-4" />
            View Audit Logs
          </CommandItem>
          <CommandItem onSelect={() => run("/settings")}>
            <Settings className="mr-2 w-4 h-4" />
            Settings
          </CommandItem>
        </CommandGroup>
        <CommandSeparator />
        <CommandGroup heading="Actions">
          <CommandItem onSelect={() => run("/reconciliation")}>
            <Zap className="mr-2 w-4 h-4" />
            Run Reconciliation
          </CommandItem>
          <CommandItem onSelect={() => run("/investigations")}>
            <Activity className="mr-2 w-4 h-4" />
            Run AI Investigations
          </CommandItem>
        </CommandGroup>
      </CommandList>
    </CommandDialog>
  )
}

// Topbar
function Topbar({ onCommandOpen }: { onCommandOpen: () => void }) {
  const { user, logout } = useAuth()
  const { theme, setTheme } = useTheme()
  const navigate = useNavigate()
  const crumbs = useBreadcrumbs()
  const { data: dashboardStats } = useDashboardStats()

  const handleLogout = () => {
    logout()
    navigate("/login")
  }

  const criticalCount = dashboardStats?.severityBreakdown?.CRITICAL ?? 0
  const hasNotifications = criticalCount > 0

  return (
    <header className="h-14 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80 flex items-center px-4 gap-4 flex-shrink-0">
      <MobileSidebar />

      {/* Breadcrumbs */}
      <div className="flex-1 hidden sm:block">
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink onClick={() => navigate("/dashboard")} className="text-xs text-muted-foreground hover:text-foreground cursor-pointer">
                FinSight
              </BreadcrumbLink>
            </BreadcrumbItem>
            {crumbs.map((crumb) => (
              <span key={crumb.path} className="flex items-center gap-1.5">
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  {crumb.isLast ? (
                    <BreadcrumbPage className="text-xs">{crumb.label}</BreadcrumbPage>
                  ) : (
                    <BreadcrumbLink onClick={() => navigate(crumb.path)} className="text-xs cursor-pointer">
                      {crumb.label}
                    </BreadcrumbLink>
                  )}
                </BreadcrumbItem>
              </span>
            ))}
          </BreadcrumbList>
        </Breadcrumb>
      </div>

      {/* Right side */}
      <div className="flex items-center gap-2 ml-auto">
        {/* Search / Command palette trigger */}
        <button
          onClick={onCommandOpen}
          className="hidden sm:flex items-center gap-2 px-3 h-8 rounded-md border border-border bg-muted/40 text-xs text-muted-foreground hover:border-border/80 hover:bg-muted/60 transition-colors cursor-pointer"
          aria-label="Open command palette"
        >
          <SearchIcon className="w-3.5 h-3.5" />
          <span>Search...</span>
          <kbd className="ml-2 px-1.5 py-0.5 text-[10px] font-mono rounded bg-background border border-border">
            ⌘K
          </kbd>
        </button>

        {/* Mobile search icon */}
        <Button
          variant="ghost"
          size="icon"
          className="sm:hidden w-8 h-8"
          onClick={onCommandOpen}
          aria-label="Search"
        >
          <SearchIcon className="w-4 h-4" />
        </Button>

        {/* Notifications */}
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <Button variant="ghost" size="icon" className="w-8 h-8 relative" aria-label="Notifications">
                <Bell className="w-4 h-4" />
                {hasNotifications && (
                  <span className="absolute top-1 right-1 w-2 h-2 bg-destructive rounded-full" />
                )}
              </Button>
            }
          />
          <DropdownMenuContent align="end" className="w-80">
            <DropdownMenuGroup>
              <DropdownMenuLabel>Notifications</DropdownMenuLabel>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            {criticalCount > 0 ? (
              <DropdownMenuGroup>
                <DropdownMenuItem onClick={() => navigate("/exceptions")}>
                  <AlertTriangle className="mr-2 w-4 h-4 text-rose-400" />
                  <div className="flex flex-col gap-0.5">
                    <span className="text-sm font-medium">{criticalCount} Critical Exception{criticalCount !== 1 ? 's' : ''}</span>
                    <span className="text-xs text-muted-foreground">Requires immediate attention</span>
                  </div>
                </DropdownMenuItem>
                {dashboardStats?.recentExceptions?.slice(0, 3).map((exp) => (
                  <DropdownMenuItem
                    key={exp.exceptionId}
                    onClick={() => navigate(`/exceptions/${exp.exceptionId}`)}
                  >
                    <div className="flex flex-col gap-0.5 w-full">
                      <span className="text-xs font-mono">{exp.exceptionId}</span>
                      <span className="text-xs text-muted-foreground truncate">{exp.exceptionType.replace(/_/g, ' ')}</span>
                    </div>
                  </DropdownMenuItem>
                ))}
              </DropdownMenuGroup>
            ) : (
              <div className="px-2 py-6 text-center text-sm text-muted-foreground">
                No new notifications
              </div>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => navigate("/exceptions")} className="text-xs">
                View all exceptions
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Theme toggle */}
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <Button variant="ghost" size="icon" className="w-8 h-8" aria-label="Toggle theme">
                {theme === "dark" ? (
                  <Moon className="w-4 h-4" />
                ) : theme === "light" ? (
                  <Sun className="w-4 h-4" />
                ) : (
                  <Monitor className="w-4 h-4" />
                )}
              </Button>
            }
          />
          <DropdownMenuContent align="end" className="w-36">
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => setTheme("light")}>
                <Sun className="mr-2 w-3.5 h-3.5" />
                Light
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setTheme("dark")}>
                <Moon className="mr-2 w-3.5 h-3.5" />
                Dark
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setTheme("system")}>
                <Monitor className="mr-2 w-3.5 h-3.5" />
                System
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>

        {/* User menu */}
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <Button variant="ghost" size="icon" className="w-8 h-8" aria-label="User menu">
                <User className="w-4 h-4" />
              </Button>
            }
          />
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuGroup>
              <DropdownMenuLabel className="font-normal">
                <div className="flex flex-col gap-1">
                  <p className="text-sm font-medium">{user?.username}</p>
                  <Badge variant="outline" className="text-[10px] w-fit">
                    {user?.role}
                  </Badge>
                </div>
              </DropdownMenuLabel>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => navigate("/settings")}>
                <Settings className="mr-2 w-3.5 h-3.5" />
                Settings
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={handleLogout} variant="destructive">
                <LogOut className="mr-2 w-3.5 h-3.5" />
                Sign out
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}

export default function AppLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [commandOpen, setCommandOpen] = useState(false)
  const location = useLocation()

  // Ctrl+K / Cmd+K shortcut
  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    if ((e.metaKey || e.ctrlKey) && e.key === "k") {
      e.preventDefault()
      setCommandOpen((v) => !v)
    }
  }, [])

  useEffect(() => {
    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [handleKeyDown])

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      <DesktopSidebar
        collapsed={sidebarCollapsed}
        onToggle={() => setSidebarCollapsed((v) => !v)}
      />

      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Topbar onCommandOpen={() => setCommandOpen(true)} />

        <main className="flex-1 overflow-y-auto relative">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
              className="h-full relative"
            >
              <div className="relative z-10">
                <Outlet />
              </div>
            </motion.div>
          </AnimatePresence>
        </main>
      </div>

      <CommandCenter open={commandOpen} onOpenChange={setCommandOpen} />
    </div>
  )
}
