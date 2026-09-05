import { useState } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { useAuth } from "@/contexts/AuthContext"
import { demoApi } from "@/api/demoApi"
import {
  PageHeader,
  SectionCard,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Switch } from "@/components/ui/switch"
import { toast } from "sonner"
import {
  User,
  Bot,
  Database,
  Sliders,
  Play,
  Lock,
  RotateCcw,
} from "lucide-react"

export default function SettingsPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [seeding, setSeeding] = useState(false)
  const [resetting, setResetting] = useState(false)
  const [validating, setValidating] = useState(false)
  const [validationReport, setValidationReport] = useState<Record<string, unknown> | null>(null)

  const handleResetData = async () => {
    setResetting(true)
    try {
      const res = await demoApi.reset()
      queryClient.invalidateQueries()
      toast.success(res.message || "Demo data reset to clean state (0 records)!")
    } catch (err: unknown) {
      const e = err as { response?: { status?: number; data?: { message?: string } }; message?: string }
      if (e?.response?.status === 403) {
        toast.error("Failed to reset demo data. Only ADMIN role can perform reset.")
      } else {
        toast.error(e?.response?.data?.message || e?.message || "Failed to reset demo data.")
      }
    } finally {
      setResetting(false)
    }
  }

  const handleSeedData = async () => {
    setSeeding(true)
    try {
      const res = await demoApi.seed()
      queryClient.invalidateQueries()
      toast.success(res.message || "Demo dataset seeded successfully!")
    } catch (err: unknown) {
      const e = err as { response?: { status?: number; data?: { message?: string } }; message?: string }
      if (e?.response?.status === 403) {
        toast.error("Failed to seed demo data. Only ADMIN role can perform seeding.")
      } else {
        toast.error(e?.response?.data?.message || e?.message || "Failed to seed demo data.")
      }
    } finally {
      setSeeding(false)
    }
  }

  const handleValidateDemo = async () => {
    setValidating(true)
    try {
      const res = await demoApi.validate()
      setValidationReport(res as unknown as Record<string, unknown>)
      toast.success(`E2E Validation Complete: Status ${res.overallStatus}`)
    } catch (err: unknown) {
      const e = err as { response?: { status?: number; data?: { message?: string } }; message?: string }
      if (e?.response?.status === 403) {
        toast.error("Validation requires ADMIN role credentials.")
      } else {
        toast.error(e?.response?.data?.message || e?.message || "Failed to execute automated demo validation suite.")
      }
    } finally {
      setValidating(false)
    }
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <PageHeader
        title="Settings & System Configuration"
        description="Manage user credentials, security role permissions, AI provider settings, and demo validation routines."
      />

      <Tabs defaultValue="profile" className="space-y-6">
        <TabsList className="bg-muted/40 border border-border p-1">
          <TabsTrigger value="profile" className="text-xs">
            <User className="w-3.5 h-3.5 mr-1.5" />
            Profile & Role
          </TabsTrigger>
          <TabsTrigger value="ai" className="text-xs">
            <Bot className="w-3.5 h-3.5 mr-1.5" />
            AI Investigation
          </TabsTrigger>
          <TabsTrigger value="demo" className="text-xs">
            <Database className="w-3.5 h-3.5 mr-1.5" />
            Demo Data & Validation
          </TabsTrigger>
          <TabsTrigger value="preferences" className="text-xs">
            <Sliders className="w-3.5 h-3.5 mr-1.5" />
            Preferences
          </TabsTrigger>
        </TabsList>

        {/* 1. Profile Tab */}
        <TabsContent value="profile" className="space-y-6">
          <SectionCard title="Active Session Credentials" description="Spring Security basic authentication context">
            <div className="max-w-md space-y-4 text-xs">
              <div className="space-y-1.5">
                <Label>Username</Label>
                <Input value={user?.username || ""} disabled className="bg-muted/40 font-mono" />
              </div>
              <div className="space-y-1.5">
                <Label>Assigned Role</Label>
                <div>
                  <Badge variant="outline" className="text-xs font-semibold text-muted-foreground border-border">
                    {user?.role}
                  </Badge>
                </div>
              </div>
            </div>
          </SectionCard>

          <SectionCard title="Role Permissions Reference" description="System capabilities per authenticated role">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
              <div className="p-4 rounded-lg border border-border bg-muted/30">
                <div className="font-semibold text-foreground mb-1">OPERATOR</div>
                <p className="text-muted-foreground mb-2">Read-only operational view across dashboard, exceptions, and audit logs.</p>
                <Badge variant="outline" className="text-[10px]">READ ONLY</Badge>
              </div>
              <div className="p-4 rounded-lg border border-border bg-muted/30">
                <div className="font-semibold text-foreground mb-1">ANALYST</div>
                <p className="text-muted-foreground mb-2">Operational access to trigger reconciliation runs and launch Gemini AI investigations.</p>
                <Badge variant="outline" className="text-[10px] text-amber-400 border-amber-500/30">OPERATIONAL</Badge>
              </div>
              <div className="p-4 rounded-lg border border-border bg-muted/30">
                <div className="font-semibold text-foreground mb-1">ADMIN</div>
                <p className="text-muted-foreground mb-2">Full administrative privileges including manual exception resolution and demo data seeding.</p>
                <Badge variant="outline" className="text-[10px] text-emerald-400 border-emerald-500/30">FULL ACCESS</Badge>
              </div>
            </div>
          </SectionCard>
        </TabsContent>

        {/* 2. AI Investigation Tab */}
        <TabsContent value="ai" className="space-y-6">
          <SectionCard title="Gemini 1.5 Flash Provider Configuration" description="AI Engine configuration & deterministic fallback settings">
            <div className="space-y-4 text-xs max-w-lg">
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/30">
                <div>
                  <div className="font-semibold text-foreground">AI Investigation Layer</div>
                  <div className="text-muted-foreground text-[11px]">Query Gemini 1.5 Flash for root cause diagnostics</div>
                </div>
                <Switch defaultChecked disabled />
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/30">
                <div>
                  <div className="font-semibold text-foreground">Deterministic Rule Fallback</div>
                  <div className="text-muted-foreground text-[11px]">Automatic fallback if AI timeout or key absent</div>
                </div>
                <Switch defaultChecked disabled />
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/30">
                <div>
                  <div className="font-semibold text-foreground">Ground Truth Amount Validation</div>
                  <div className="text-muted-foreground text-[11px]">Enforce strict cross-check against DB figures</div>
                </div>
                <Switch defaultChecked disabled />
              </div>
            </div>
          </SectionCard>
        </TabsContent>

        {/* 3. Demo Data & Validation Tab */}
        <TabsContent value="demo" className="space-y-6">
          <SectionCard title="Demo Controls & Seed Scenarios" description="Populate application database with 10 deterministic test cases">
            <div className="space-y-4">
              <p className="text-xs text-muted-foreground">
                Seeding creates idempotent order, payment, fee, refund, and settlement records covering 8 distinct exception types.
              </p>

              <div className="flex items-center gap-3">
                <Button
                  size="sm"
                  onClick={handleSeedData}
                  disabled={seeding || resetting || user?.role !== "ADMIN"}
                  className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
                >
                  <Database className="w-3.5 h-3.5 mr-1.5" />
                  {seeding ? "Seeding Data..." : "Seed 10 Demo Scenarios"}
                </Button>

                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleResetData}
                  disabled={seeding || resetting || user?.role !== "ADMIN"}
                  className="text-xs border-destructive/40 text-destructive hover:bg-destructive/10"
                >
                  <RotateCcw className="w-3.5 h-3.5 mr-1.5" />
                  {resetting ? "Resetting..." : "Reset to Clean Slate (0 Records)"}
                </Button>

                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleValidateDemo}
                  disabled={validating || user?.role !== "ADMIN"}
                  className="text-xs border-border text-foreground"
                >
                  <Play className="w-3.5 h-3.5 mr-1.5" />
                  {validating ? "Running Suite..." : "Run E2E Demo Validation"}
                </Button>
              </div>

              {user?.role !== "ADMIN" && (
                <p className="text-[11px] text-amber-400 flex items-center gap-1">
                  <Lock className="w-3 h-3" /> Seeding requires ADMIN role credentials.
                </p>
              )}
            </div>
          </SectionCard>

          {validationReport && (
            <SectionCard title="Validation Report Output" description="Latest E2E validation run status">
              <pre className="text-[11px] font-mono p-4 rounded-lg bg-muted/40 border border-border text-muted-foreground overflow-x-auto">
                {String(JSON.stringify(validationReport, null, 2))}
              </pre>
            </SectionCard>
          )}
        </TabsContent>

        {/* 4. Preferences Tab */}
        <TabsContent value="preferences" className="space-y-6">
          <SectionCard title="UI Preferences" description="Customize interface theme and behavior">
            <div className="space-y-4 text-xs max-w-md">
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/30">
                <div>
                  <div className="font-semibold text-foreground">Compact Lineage Graphs</div>
                  <div className="text-muted-foreground text-[11px]">Use streamlined graph nodes</div>
                </div>
                <Switch defaultChecked />
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/30">
                <div>
                  <div className="font-semibold text-foreground">Auto Refresh Analytics</div>
                  <div className="text-muted-foreground text-[11px]">Poll stats every 30 seconds</div>
                </div>
                <Switch defaultChecked />
              </div>
            </div>
          </SectionCard>
        </TabsContent>
      </Tabs>
    </div>
  )
}
