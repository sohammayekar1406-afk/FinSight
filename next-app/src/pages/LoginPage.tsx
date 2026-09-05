import { useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { useAuth } from "@/contexts/AuthContext"

// ─── Validation schema ────────────────────────────────────────────────────────
const loginSchema = z.object({
  username: z.string().min(1, "Username is required"),
  password: z.string().min(1, "Password is required"),
  rememberMe: z.boolean().optional(),
})
type LoginForm = z.infer<typeof loginSchema>

// ─── Tiny sub-components ─────────────────────────────────────────────────────

function PulseDot() {
  return (
    <span className="login-pulse-dot" />
  )
}

function WaveformBars() {
  return (
    <div className="flex items-end gap-[3px] h-4">
      {[40, 80, 60, 100, 30, 70, 50].map((h, i) => (
        <div
          key={i}
          className="w-[3px] bg-white rounded-full opacity-40"
          style={{ height: `${h}%` }}
        />
      ))}
    </div>
  )
}

function ExceptionQueuePreview() {
  const items = [
    { id: "SET-044", desc: "₹150.00 settlement mismatch", level: "HIGH", dot: "#ef4444" },
    { id: "INV-902", desc: "Duplicate payment detected", level: "MED", dot: "#f97316" },
    { id: "ORD-217", desc: "Missing refund linkage", level: "LOW", dot: "#eab308" },
  ]
  return (
    <div className="w-full border border-white/[0.08] bg-[#0d0d0d] p-3.5 text-left">
      <div className="flex justify-between items-center mb-3">
        <span className="font-mono text-[10px] text-white/40 tracking-[0.12em] uppercase">
          Exception Queue
        </span>
        <span className="text-[9px] px-1.5 py-0.5 border border-red-500/30 text-red-400 font-mono">
          {items.length} OPEN
        </span>
      </div>
      <div className="flex flex-col gap-2">
        {items.map((item) => (
          <div key={item.id} className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div
                className="w-1.5 h-1.5 rounded-full flex-shrink-0"
                style={{ backgroundColor: item.dot }}
              />
              <span className="font-mono text-[11px] text-white/80">
                {item.id} · {item.desc}
              </span>
            </div>
            <span className="text-[9px] px-1 border border-white/10 text-white/40 font-mono flex-shrink-0 ml-2">
              {item.level}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── Input field ─────────────────────────────────────────────────────────────

interface InputFieldProps {
  label: string
  type?: string
  placeholder: string
  icon: string
  error?: string
  rightSlot?: React.ReactNode
  registration: ReturnType<ReturnType<typeof useForm<LoginForm>>["register"]>
}

function InputField({
  label,
  type = "text",
  placeholder,
  icon,
  error,
  rightSlot,
  registration,
}: InputFieldProps) {
  return (
    <div className="flex flex-col gap-2">
      <label className="font-mono text-[10px] tracking-[0.12em] text-white/40 uppercase">
        {label}
      </label>
      <div className="relative flex items-center border-b border-white/10 focus-within:border-white pb-2 transition-colors duration-200">
        <span className="material-symbols-outlined text-white/30 text-[18px] mr-2.5 select-none">
          {icon}
        </span>
        <input
          {...registration}
          type={type}
          placeholder={placeholder}
          className="bg-transparent text-white w-full font-mono text-sm focus:outline-none placeholder:text-white/20"
        />
        {rightSlot}
      </div>
      {error && (
        <p className="text-[11px] text-red-400 font-mono">{error}</p>
      )}
    </div>
  )
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from =
    (location.state as { from?: { pathname: string } })?.from?.pathname ?? "/dashboard"

  const [overlayRight, setOverlayRight] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [banner, setBanner] = useState<"success" | "error" | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: "", password: "", rememberMe: false },
  })

  const onSubmit = async (data: LoginForm) => {
    setError(null)
    setBanner(null)
    try {
      await login(data.username, data.password)
      setBanner("success")
      setTimeout(() => navigate(from, { replace: true }), 600)
    } catch (err: unknown) {
      const e = err as { response?: { status?: number } }
      if (e?.response?.status === 401) {
        setError("Invalid username or password.")
      } else {
        setError("Cannot reach server. Ensure the backend is running on port 8080.")
      }
      setBanner("error")
      setTimeout(() => setBanner(null), 3000)
    }
  }

  return (
    <>
      {/* Google Fonts */}
      <link
        href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@300,0&display=swap"
        rel="stylesheet"
      />

      <div className="login-root">
        {/* ── Dot-grid background ── */}
        <div className="login-dot-grid" />
        <div className="login-ambient-glow" />

        {/* ── Banner ── */}
        {banner && (
          <div className={`login-banner login-banner--${banner}`}>
            <span className="material-symbols-outlined text-[16px]">
              {banner === "success" ? "check_circle" : "error"}
            </span>
            {banner === "success"
              ? "Authentication successful. Redirecting…"
              : error}
          </div>
        )}

        {/* ── Card ── */}
        <div className={`login-card ${overlayRight ? "login-card--signup-active" : ""}`}>

          {/* ── Sign-in panel ── */}
          <div className="login-form-panel login-form-panel--left">
            <SignInPanel
              register={register}
              handleSubmit={handleSubmit}
              errors={errors}
              isSubmitting={isSubmitting}
              onSubmit={onSubmit}
              showPassword={showPassword}
              setShowPassword={setShowPassword}
              onSwitchToSignUp={() => setOverlayRight(true)}
            />
          </div>

          {/* ── Sign-up panel ── */}
          <div className="login-form-panel login-form-panel--right">
            <SignUpPanel onSwitchToSignIn={() => setOverlayRight(false)} />
          </div>

          {/* ── Overlay ── */}
          <div className="login-overlay-container">
            <div className="login-overlay">
              {/* Left overlay (shown when on sign-up) */}
              <div className="login-overlay-panel login-overlay-panel--left">
                <OverlayBrandLeft onSignIn={() => setOverlayRight(false)} />
              </div>
              {/* Right overlay (shown when on sign-in) */}
              <div className="login-overlay-panel login-overlay-panel--right">
                <OverlayBrandRight onSignUp={() => setOverlayRight(true)} />
              </div>
            </div>
          </div>
        </div>

        {/* ── Mobile view: single sign-in form ── */}
        <div className="login-mobile">
          <MobileSignIn
            register={register}
            handleSubmit={handleSubmit}
            errors={errors}
            isSubmitting={isSubmitting}
            onSubmit={onSubmit}
            showPassword={showPassword}
            setShowPassword={setShowPassword}
          />
        </div>
      </div>
    </>
  )
}

// ─── Sign-in panel content ────────────────────────────────────────────────────

interface SignInPanelProps {
  register: ReturnType<typeof useForm<LoginForm>>["register"]
  handleSubmit: ReturnType<typeof useForm<LoginForm>>["handleSubmit"]
  errors: ReturnType<typeof useForm<LoginForm>>["formState"]["errors"]
  isSubmitting: boolean
  onSubmit: (data: LoginForm) => Promise<void>
  showPassword: boolean
  setShowPassword: (v: boolean) => void
  onSwitchToSignUp?: () => void
}

function SignInPanel({
  register,
  handleSubmit,
  errors,
  isSubmitting,
  onSubmit,
  showPassword,
  setShowPassword,
}: SignInPanelProps) {
  return (
    <div className="flex flex-col h-full justify-center p-14 xl:p-16">
      {/* Heading */}
      <div className="mb-6">
        <p className="font-mono text-[10px] tracking-[0.14em] text-white/30 uppercase mb-2">
          Financial Intelligence Platform
        </p>
        <h2 className="login-display-heading">Sign In</h2>
        <p className="font-mono text-[11px] text-white/40 mt-1">
          Demo Sandbox: Connects to shared demo workspace <span className="text-emerald-400 font-mono">Merchant A</span>.
        </p>
      </div>

      {/* Preset credentials callout */}
      <div className="mb-6 p-3 rounded-lg border border-white/10 bg-white/[0.03] space-y-1.5 font-mono text-[11px]">
        <div className="text-white/60 text-[10px] uppercase tracking-wider">Demo Evaluator Accounts:</div>
        <div className="flex flex-wrap gap-2 text-white/80 text-[11px]">
          <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10">admin / admin123</span>
          <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10">analyst / analyst123</span>
          <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10">operator / operator123</span>
        </div>
        <div className="text-white/40 text-[10px] pt-1">
          Isolation test: <span className="text-white/60">merchant_b_admin / admin123</span>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
        <InputField
          label="Username"
          placeholder="admin"
          icon="person"
          error={errors.username?.message}
          registration={register("username")}
        />

        <InputField
          label="Password"
          type={showPassword ? "text" : "password"}
          placeholder="••••••••"
          icon="lock"
          error={errors.password?.message}
          registration={register("password")}
          rightSlot={
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="text-white/30 hover:text-white/70 transition-colors ml-2 focus:outline-none"
              tabIndex={-1}
            >
              <span className="material-symbols-outlined text-[18px]">
                {showPassword ? "visibility_off" : "visibility"}
              </span>
            </button>
          }
        />

        <div className="flex items-center gap-2 mt-1">
          <input
            {...register("rememberMe")}
            id="rememberMe"
            type="checkbox"
            className="w-3.5 h-3.5 bg-transparent border border-white/20 rounded-none accent-white focus:ring-0 focus:ring-offset-0"
          />
          <label
            htmlFor="rememberMe"
            className="font-mono text-[11px] text-white/30 cursor-pointer"
          >
            Remember me on this device
          </label>
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="login-btn-primary mt-2"
        >
          {isSubmitting ? (
            <span className="flex items-center justify-center gap-2">
              <svg
                className="animate-spin h-4 w-4"
                viewBox="0 0 24 24"
                fill="none"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                />
              </svg>
              Authenticating…
            </span>
          ) : (
            <span className="flex items-center justify-center gap-2">
              Access Dashboard
              <span className="material-symbols-outlined text-[16px] login-btn-arrow">
                arrow_forward
              </span>
            </span>
          )}
        </button>
      </form>

      {/* OAuth row */}
      <div className="flex gap-3 mt-7">
        <OAuthButton icon="github" label="GitHub" />
        <OAuthButton icon="google" label="Google" />
      </div>
    </div>
  )
}

// ─── Sign-up schema ───────────────────────────────────────────────────────────
// The backend uses HTTP Basic Auth with fixed in-memory users (admin/analyst/operator).
// There is no registration endpoint. "Sign Up" authenticates an existing account.

const signUpSchema = z.object({
  displayName: z.string().min(1, "Name is required"),
  username: z.string().min(1, "Username is required"),
  password: z.string().min(1, "Password is required"),
})
type SignUpForm = z.infer<typeof signUpSchema>

// ─── Sign-up panel ────────────────────────────────────────────────────────────

function SignUpPanel({ onSwitchToSignIn }: { onSwitchToSignIn: () => void }) {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? "/dashboard"

  const [showPw, setShowPw] = useState(false)
  const [signUpError, setSignUpError] = useState<string | null>(null)

  const {
    register: reg,
    handleSubmit: handleSU,
    formState: { errors: suErrors, isSubmitting: suSubmitting },
  } = useForm<SignUpForm>({
    resolver: zodResolver(signUpSchema),
    defaultValues: { displayName: "", username: "", password: "" },
  })

  const onSignUp = async (data: SignUpForm) => {
    setSignUpError(null)
    try {
      // Authenticate with the existing backend using the provided credentials.
      // The backend uses in-memory HTTP Basic Auth — no separate register endpoint exists.
      await login(data.username, data.password)
      navigate(from, { replace: true })
    } catch (err: unknown) {
      const e = err as { response?: { status?: number } }
      if (e?.response?.status === 401) {
        setSignUpError("Invalid credentials. Use: admin / admin123, analyst / analyst123, or operator / operator123.")
      } else {
        setSignUpError("Cannot reach server. Start the backend: cd FinSight && mvnw spring-boot:run")
      }
    }
  }

  return (
    <div className="flex flex-col h-full justify-center p-14 xl:p-16">
      <div className="mb-6">
        <p className="font-mono text-[10px] tracking-[0.14em] text-white/30 uppercase mb-2">
          Financial Intelligence Platform
        </p>
        <h2 className="login-display-heading">Sign Up</h2>
        <p className="font-mono text-[11px] text-white/40 mt-1">
          Demo Sandbox: Connects to shared demo workspace <span className="text-emerald-400 font-mono">Merchant A</span>.
        </p>
      </div>

      {/* Shared sandbox notice */}
      <div className="mb-6 p-3 rounded-lg border border-white/10 bg-white/[0.03] space-y-1.5 font-mono text-[11px]">
        <div className="text-white/60 text-[10px] uppercase tracking-wider">Shared Sandbox Notice:</div>
        <p className="text-white/50 text-[10px] leading-relaxed">
          FinSight uses preset evaluator personas. To evaluate the platform, sign in with one of the standard demo accounts:
        </p>
        <div className="flex flex-wrap gap-2 text-white/80 text-[11px] pt-1">
          <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10">admin / admin123</span>
          <span className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10">analyst / analyst123</span>
        </div>
      </div>

      {signUpError && (
        <div className="mb-5 border border-red-500/30 bg-red-500/5 px-3 py-2.5 flex items-start gap-2">
          <span className="material-symbols-outlined text-red-400 text-[16px] flex-shrink-0 mt-0.5">
            error
          </span>
          <p className="font-mono text-[11px] text-red-400 leading-relaxed">{signUpError}</p>
        </div>
      )}

      <form className="flex flex-col gap-6" onSubmit={handleSU(onSignUp)}>
        {/* Full Name — UI only, not sent to backend */}
        <div className="flex flex-col gap-2">
          <label className="font-mono text-[10px] tracking-[0.12em] text-white/40 uppercase">
            Full Name
          </label>
          <div className="flex items-center border-b border-white/10 focus-within:border-white pb-2 transition-colors duration-200">
            <span className="material-symbols-outlined text-white/30 text-[18px] mr-2.5 select-none">
              person
            </span>
            <input
              {...reg("displayName")}
              type="text"
              placeholder="John Doe"
              className="bg-transparent text-white w-full font-mono text-sm focus:outline-none placeholder:text-white/20"
            />
          </div>
          {suErrors.displayName && (
            <p className="font-mono text-[11px] text-red-400">{suErrors.displayName.message}</p>
          )}
        </div>

        {/* Username — maps to backend username */}
        <div className="flex flex-col gap-2">
          <label className="font-mono text-[10px] tracking-[0.12em] text-white/40 uppercase">
            Username
          </label>
          <div className="flex items-center border-b border-white/10 focus-within:border-white pb-2 transition-colors duration-200">
            <span className="material-symbols-outlined text-white/30 text-[18px] mr-2.5 select-none">
              person
            </span>
            <input
              {...reg("username")}
              type="text"
              autoComplete="username"
              placeholder="admin · analyst · operator"
              className="bg-transparent text-white w-full font-mono text-sm focus:outline-none placeholder:text-white/20"
            />
          </div>
          {suErrors.username && (
            <p className="font-mono text-[11px] text-red-400">{suErrors.username.message}</p>
          )}
        </div>

        {/* Password */}
        <div className="flex flex-col gap-2">
          <label className="font-mono text-[10px] tracking-[0.12em] text-white/40 uppercase">
            Password
          </label>
          <div className="flex items-center border-b border-white/10 focus-within:border-white pb-2 transition-colors duration-200">
            <span className="material-symbols-outlined text-white/30 text-[18px] mr-2.5 select-none">
              lock
            </span>
            <input
              {...reg("password")}
              type={showPw ? "text" : "password"}
              placeholder="••••••••"
              className="bg-transparent text-white w-full font-mono text-sm focus:outline-none placeholder:text-white/20"
            />
            <button
              type="button"
              onClick={() => setShowPw(!showPw)}
              className="text-white/30 hover:text-white/70 transition-colors ml-2 focus:outline-none"
              tabIndex={-1}
            >
              <span className="material-symbols-outlined text-[18px]">
                {showPw ? "visibility_off" : "visibility"}
              </span>
            </button>
          </div>
          {suErrors.password && (
            <p className="font-mono text-[11px] text-red-400">{suErrors.password.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={suSubmitting}
          className="login-btn-primary mt-2"
        >
          {suSubmitting ? (
            <span className="flex items-center justify-center gap-2">
              <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              Authenticating…
            </span>
          ) : (
            <span className="flex items-center justify-center gap-2">
              Create Account
              <span className="material-symbols-outlined text-[16px] login-btn-arrow">
                arrow_forward
              </span>
            </span>
          )}
        </button>
      </form>

      <button
        onClick={onSwitchToSignIn}
        className="mt-6 text-center font-mono text-[11px] text-white/30 hover:text-white/60 transition-colors"
      >
        Already have an account? Sign in →
      </button>
    </div>
  )
}

// ─── Overlay panels ───────────────────────────────────────────────────────────

function OverlayBrandLeft({ onSignIn }: { onSignIn: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center h-full px-10 text-center gap-8">
      <div>
        <h1 className="login-display-heading text-center">FinSight</h1>
        <p className="font-mono text-[12px] text-white/40 mt-2">
          See every discrepancy <em>before</em> it becomes a problem.
        </p>
      </div>
      <button onClick={onSignIn} className="login-btn-outline">
        Already have an account? Sign In
      </button>
    </div>
  )
}

function OverlayBrandRight({ onSignUp }: { onSignUp: () => void }) {
  return (
    <div className="flex flex-col items-center justify-between h-full py-14 px-10">
      {/* Top brand */}
      <div className="text-center">
        <h1 className="login-display-heading text-center">FinSight</h1>
        <p className="font-mono text-[12px] text-white/40 mt-2">
          Precision analytics for <em>elite</em> finance teams.
        </p>
      </div>

      {/* Middle widgets */}
      <div className="flex flex-col gap-4 w-full">
        <button onClick={onSignUp} className="login-btn-outline w-full">
          New here? Create an account
        </button>
        <ExceptionQueuePreview />
      </div>

      {/* Bottom status strip */}
      <div className="w-full flex flex-col gap-3">
        <div className="flex items-center justify-between border-b border-white/[0.06] pb-3">
          <div className="flex items-center gap-2 font-mono text-[10px] text-white/40 tracking-[0.1em] uppercase">
            <PulseDot />
            System Active
          </div>
          <WaveformBars />
        </div>
        <div className="flex justify-between">
          <div>
            <div className="font-mono text-[10px] text-white/30 tracking-[0.1em] uppercase mb-1">
              Match Rate
            </div>
            <div className="font-mono text-white text-sm">99.8%</div>
          </div>
          <div className="text-right">
            <div className="font-mono text-[10px] text-white/30 tracking-[0.1em] uppercase mb-1">
              Open Exceptions
            </div>
            <div className="font-mono text-white text-sm">3</div>
          </div>
        </div>
      </div>
    </div>
  )
}

// ─── Mobile-only sign-in ──────────────────────────────────────────────────────

function MobileSignIn({
  register,
  handleSubmit,
  errors,
  isSubmitting,
  onSubmit,
  showPassword,
  setShowPassword,
}: Omit<SignInPanelProps, "onSwitchToSignUp">) {
  return (
    <div className="flex flex-col w-full max-w-sm mx-auto px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="login-display-heading text-center mb-1">FinSight</h1>
        <p className="font-mono text-[11px] text-white/30">
          See every discrepancy <em>before</em> it becomes a problem.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
        <InputField
          label="Username"
          placeholder="admin"
          icon="person"
          error={errors.username?.message}
          registration={register("username")}
        />
        <InputField
          label="Password"
          type={showPassword ? "text" : "password"}
          placeholder="••••••••"
          icon="lock"
          error={errors.password?.message}
          registration={register("password")}
          rightSlot={
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="text-white/30 hover:text-white/70 transition-colors ml-2"
              tabIndex={-1}
            >
              <span className="material-symbols-outlined text-[18px]">
                {showPassword ? "visibility_off" : "visibility"}
              </span>
            </button>
          }
        />
        <button
          type="submit"
          disabled={isSubmitting}
          className="login-btn-primary mt-1"
        >
          {isSubmitting ? (
            <span className="flex items-center justify-center gap-2">
              <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              Authenticating…
            </span>
          ) : (
            <span className="flex items-center justify-center gap-2">
              Access Dashboard
              <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
            </span>
          )}
        </button>
      </form>
    </div>
  )
}

// ─── OAuth icon components (declared at module scope to satisfy react-hooks/static-components) ──

function GitHubIcon() {
  return (
    <svg viewBox="0 0 24 24" className="w-3.5 h-3.5 fill-current">
      <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
    </svg>
  )
}

function GoogleIcon() {
  return (
    <svg viewBox="0 0 24 24" className="w-3.5 h-3.5">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  )
}

// ─── OAuth placeholder button ─────────────────────────────────────────────────

function OAuthButton({ icon, label }: { icon: "github" | "google"; label: string }) {
  return (
    <button className="login-oauth-btn group flex-1">
      {icon === "github" ? <GitHubIcon /> : <GoogleIcon />}
      <span>{label}</span>
    </button>
  )
}
