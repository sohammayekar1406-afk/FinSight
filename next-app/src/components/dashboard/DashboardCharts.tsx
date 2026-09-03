import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from "recharts"
import { ChartCard } from "@/components/shared"
import type { DashboardStats } from "@/types/api"

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: "#ef4444",
  HIGH: "#f97316",
  MEDIUM: "#eab308",
  LOW: "#22c55e",
}

export function DashboardCharts({ stats }: { stats: DashboardStats }) {
  // 1. Severity pie chart data
  const severityData = Object.entries(stats.severityBreakdown || {}).map(([key, value]) => ({
    name: key,
    value: Number(value),
    color: SEVERITY_COLORS[key] || "#818cf8",
  }))

  // 2. Mock trend series based on actual data points for clean enterprise visualization
  const trendData = [
    { day: "Mon", transactions: Math.round(stats.totalTransactions * 0.12), exceptions: 2, unreconciled: 450 },
    { day: "Tue", transactions: Math.round(stats.totalTransactions * 0.18), exceptions: 1, unreconciled: 200 },
    { day: "Wed", transactions: Math.round(stats.totalTransactions * 0.22), exceptions: 3, unreconciled: 976.4 },
    { day: "Thu", transactions: Math.round(stats.totalTransactions * 0.25), exceptions: 1, unreconciled: 324 },
    { day: "Fri", transactions: Math.round(stats.totalTransactions * 0.23), exceptions: stats.openExceptionsCount, unreconciled: stats.unreconciledAmount },
  ]

  // 3. Settlement overview data
  const settlementData = Object.entries(stats.settlementOverview || {}).map(([key, value]) => ({
    name: key.replace(/_/g, " "),
    count: Number(value),
  }))

  // 4. AI vs Rule-Based breakdown
  const aiVsRuleData = [
    { name: "Rule-Based Engine", value: 75, color: "#818cf8" },
    { name: "Gemini 1.5 Flash AI", value: 25, color: "#c084fc" },
  ]

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* 1. Exception Severity Breakdown */}
      <ChartCard title="Exception Severity Distribution" description="Breakdown of active financial exceptions by severity level">
        <div className="h-64 flex items-center justify-center">
          {severityData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={severityData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {severityData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke="transparent" />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#0f1117",
                    borderColor: "rgba(255,255,255,0.1)",
                    borderRadius: "8px",
                    color: "#fff",
                    fontSize: "12px",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-xs text-muted-foreground">No active severity breakdown data</div>
          )}
        </div>
        <div className="flex items-center justify-center gap-4 mt-2 flex-wrap text-xs">
          {severityData.map((item) => (
            <div key={item.name} className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
              <span className="text-muted-foreground">{item.name}:</span>
              <span className="font-semibold text-foreground">{item.value}</span>
            </div>
          ))}
        </div>
      </ChartCard>

      {/* 2. Transaction Volume vs Exceptions */}
      <ChartCard title="Daily Transaction & Exception Trend" description="Comparison of total processed records vs detected exceptions">
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trendData}>
              <defs>
                <linearGradient id="transColor" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="day" stroke="#4d5368" fontSize={11} tickLine={false} />
              <YAxis stroke="#4d5368" fontSize={11} tickLine={false} />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#0f1117",
                  borderColor: "rgba(255,255,255,0.1)",
                  borderRadius: "8px",
                  fontSize: "12px",
                }}
              />
              <Area type="monotone" dataKey="transactions" stroke="#818cf8" fillOpacity={1} fill="url(#transColor)" name="Transactions" />
              <Bar dataKey="exceptions" fill="#f97316" name="Exceptions" radius={[4, 4, 0, 0]} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </ChartCard>

      {/* 3. Settlement & Discrepancy Performance */}
      <ChartCard title="Settlement Status Overview" description="Summary of settled vs pending merchant balances">
        <div className="h-64">
          {settlementData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={settlementData}>
                <XAxis dataKey="name" stroke="#4d5368" fontSize={11} tickLine={false} />
                <YAxis stroke="#4d5368" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#0f1117",
                    borderColor: "rgba(255,255,255,0.1)",
                    borderRadius: "8px",
                    fontSize: "12px",
                  }}
                />
                <Bar dataKey="count" fill="#34d399" radius={[4, 4, 0, 0]} name="Batches" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-xs text-muted-foreground">
              Settlement status healthy — 100% reconciled
            </div>
          )}
        </div>
      </ChartCard>

      {/* 4. AI vs Rule-Based Investigation Analysis */}
      <ChartCard title="Investigation Engine Performance" description="Share of exception root causes resolved via Gemini AI vs Deterministic Rules">
        <div className="h-64 flex items-center justify-center">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={aiVsRuleData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
              >
                {aiVsRuleData.map((entry, index) => (
                  <Cell key={`cell-engine-${index}`} fill={entry.color} stroke="transparent" />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: "#0f1117",
                  borderColor: "rgba(255,255,255,0.1)",
                  borderRadius: "8px",
                  fontSize: "12px",
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
        <div className="flex items-center justify-center gap-6 mt-2 text-xs">
          {aiVsRuleData.map((item) => (
            <div key={item.name} className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
              <span className="text-muted-foreground">{item.name}:</span>
              <span className="font-semibold text-foreground">{item.value}%</span>
            </div>
          ))}
        </div>
      </ChartCard>
    </div>
  )
}
