import { useState, useEffect, useRef } from "react"
import { useNavigate, Link } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import { motion, AnimatePresence } from "motion/react"
import { Sparkles, X, Check } from "lucide-react"
import { Button } from "@/components/ui/button"

// ─── Scroll-reveal hook ───────────────────────────────────────────────────────
// Adds .is-visible to every .reveal-item once it enters the viewport
function useScrollReveal() {
  useEffect(() => {
    const items = document.querySelectorAll<HTMLElement>(".reveal-item")
    if (!items.length) return

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible")
            observer.unobserve(entry.target) // fire once
          }
        })
      },
      { threshold: 0.12, rootMargin: "0px 0px -40px 0px" }
    )

    items.forEach((el) => observer.observe(el))
    return () => observer.disconnect()
  }, [])
}

declare global {
  interface Window {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    Hls: any;
  }
}

export default function LandingPage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  // ── Scroll-reveal ──────────────────────────────────────────
  useScrollReveal()

  // Accordion state — collapsed until the user clicks a card
  const [expandedCards, setExpandedCards] = useState<Record<number, boolean>>({})

  // Demo Modal State
  const [demoOpen, setDemoOpen] = useState(false)
  const [demoEmail, setDemoEmail] = useState("")
  const [demoSubmitted, setDemoSubmitted] = useState(false)

  // SVG lines state & ref
  const svgRef = useRef<SVGSVGElement | null>(null)

  const toggleAccordion = (index: number) => {
    setExpandedCards(prev => ({
      ...prev,
      [index]: !prev[index]
    }))
  }

  // Draw connecting lines dynamically based on card positions
  useEffect(() => {
    const svg = svgRef.current
    if (!svg) return

    const drawLines = () => {
      const group = svg.querySelector(".lines-group")
      const cards = document.querySelectorAll(".catch-card")
      if (!group || cards.length === 0) return

      group.innerHTML = ""

      if (window.innerWidth < 768) {
        return
      }

      const svgRect = svg.getBoundingClientRect()
      // Center of the hub circle in SVG viewBox coordinates (1000 × 800)
      const centerX = 1000 * 0.5
      const centerY = 800 * 0.5
      // Radius of the hub circle outline in SVG units (matching the ring around the heading)
      const hubRadius = 90

      cards.forEach((card, i) => {
        const cardRect = card.getBoundingClientRect()
        const cardCenterX = cardRect.left - svgRect.left + cardRect.width / 2
        const cardCenterY = cardRect.top - svgRect.top + cardRect.height / 2
        const centerSvgX = svgRect.width * 0.5
        const centerSvgY = svgRect.height * 0.5

        const dx = cardCenterX - centerSvgX
        const dy = cardCenterY - centerSvgY

        // Find intersection of line with card's bounding box boundary
        const hw = cardRect.width / 2
        const hh = cardRect.height / 2
        const scaleX = Math.abs(dx) > 0.001 ? hw / Math.abs(dx) : 1000
        const scaleY = Math.abs(dy) > 0.001 ? hh / Math.abs(dy) : 1000
        const scale = Math.min(scaleX, scaleY)

        const edgePixelX = cardCenterX - dx * scale
        const edgePixelY = cardCenterY - dy * scale

        const targetX = (edgePixelX / svgRect.width) * 1000
        const targetY = (edgePixelY / svgRect.height) * 800

        // Calculate start point on the edge of the center hub circle
        const vecX = targetX - centerX
        const vecY = targetY - centerY
        const dist = Math.sqrt(vecX * vecX + vecY * vecY)
        const startX = dist > 0 ? centerX + (vecX / dist) * hubRadius : centerX
        const startY = dist > 0 ? centerY + (vecY / dist) * hubRadius : centerY

        const path = document.createElementNS("http://www.w3.org/2000/svg", "path")
        const d = `M ${startX} ${startY} L ${targetX} ${targetY}`

        path.setAttribute("d", d)
        path.setAttribute("stroke", "white")
        path.setAttribute("stroke-width", "1")
        path.setAttribute("fill", "none")
        path.setAttribute("opacity", "0.25")
        path.setAttribute("filter", "url(#glow)")

        const length = Math.sqrt(Math.pow(targetX - startX, 2) + Math.pow(targetY - startY, 2))
        path.style.strokeDasharray = `${length}`
        path.style.strokeDashoffset = `${length}`
        path.style.transition = `stroke-dashoffset 0.8s ease-out ${i * 60}ms`

        group.appendChild(path)

        // Trigger animation
        requestAnimationFrame(() => {
          path.style.strokeDashoffset = "0"
        })
      })
    }

    // Run on intersection observer or delay to ensure cards are rendered
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        drawLines()
        // Keep resize listener but disconnect observer once initial lines are drawn
        observer.disconnect()
      }
    }, { threshold: 0.1 })

    observer.observe(svg)

    window.addEventListener("resize", drawLines)
    return () => {
      window.removeEventListener("resize", drawLines)
      observer.disconnect()
    }
  }, [])

  // Initialize videos via Hls.js (Mux streaming links)
  useEffect(() => {
    const initVideo = (videoEl: HTMLVideoElement | null, src: string) => {
      if (!videoEl) return
      if (window.Hls && window.Hls.isSupported()) {
        const hls = new window.Hls()
        hls.loadSource(src)
        hls.attachMedia(videoEl)
      } else if (videoEl.canPlayType("application/vnd.apple.mpegurl")) {
        videoEl.src = src
      }
    }

    const uiVideo = document.getElementById("ui-video") as HTMLVideoElement | null
    initVideo(uiVideo, "https://stream.mux.com/Jwr2RhmsNrd6GEspBNgm02vJsRZAGlaoQIh4AucGdASw.m3u8")

    const transitionVideo = document.getElementById("transition-video") as HTMLVideoElement | null
    initVideo(transitionVideo, "https://stream.mux.com/3gErUdcrPfibrZ00ysHSLAupEL01PeX4PpAwgcGpGvbAM.m3u8")
  }, [])

  const handleDemoSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!demoEmail) return
    setDemoSubmitted(true)
    setTimeout(() => {
      setDemoOpen(false)
      setDemoSubmitted(false)
      setDemoEmail("")
    }, 2000)
  }

  return (
    <div className="antialiased selection:bg-white selection:text-black min-h-screen flex flex-col bg-[#0A0A0A] text-white font-sans">

      {/* 1. TOP NAV */}
      <nav className="absolute top-0 left-0 w-full z-50 flex justify-between items-center px-8 md:px-16 py-6 bg-transparent">
        <Link to="/" className="font-headline-sm text-headline-sm font-bold text-white tracking-tight hover:opacity-90 transition-opacity">
          FinSight
        </Link>
        <div className="hidden md:flex items-center space-x-2 relative pb-1">
          <a className="nav-link group relative text-zinc-400 font-body-md text-body-md hover:text-white px-4 py-2 transition-all duration-200" href="#product">
            Platform
            <span className="absolute bottom-0 left-1/2 w-0 h-[1px] bg-white -translate-x-1/2 transition-all duration-250 ease-out group-hover:w-1/2"></span>
          </a>
          <a className="nav-link group relative text-zinc-400 font-body-md text-body-md hover:text-white px-4 py-2 transition-all duration-200" href="#features">
            Solutions
            <span className="absolute bottom-0 left-1/2 w-0 h-[1px] bg-white -translate-x-1/2 transition-all duration-250 ease-out group-hover:w-1/2"></span>
          </a>
          <a className="nav-link group relative text-zinc-400 font-body-md text-body-md hover:text-white px-4 py-2 transition-all duration-200" href="#architecture">
            Developers
            <span className="absolute bottom-0 left-1/2 w-0 h-[1px] bg-white -translate-x-1/2 transition-all duration-250 ease-out group-hover:w-1/2"></span>
          </a>
        </div>

        <div className="flex items-center space-x-4">
          {!isAuthenticated ? (
            <Link to="/login" className="font-body-md text-body-md text-white hover:opacity-80 transition-opacity">
              Login
            </Link>
          ) : (
            <Link to="/dashboard" className="font-body-md text-body-md text-white hover:opacity-80 transition-opacity">
              Dashboard
            </Link>
          )}
          <button
            onClick={() => isAuthenticated ? navigate("/dashboard") : navigate("/login")}
            className="bg-white text-black font-body-md text-body-md px-6 py-3 rounded-none hover:bg-zinc-200 transition-colors"
          >
            Get Started
          </button>
        </div>
      </nav>

      <main className="flex-grow">

        {/* 2. HERO SECTION */}
        <section id="product" className="relative w-full min-h-[90vh] py-24 pt-[160px] flex items-center justify-center overflow-hidden">
          {/* Background Video */}
          <div className="absolute inset-0 z-0 overflow-hidden">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="absolute inset-0 w-full h-full object-cover opacity-[0.7]"
            >
              <source src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260806_133255_956f653f-5d80-4b06-abd5-0f46c98b60fa.mp4" type="video/mp4" />
            </video>
            {/* Gradient Overlay */}
            <div
              className="absolute inset-0"
              style={{
                background: "linear-gradient(to bottom, transparent 60%, #0A0A0A 100%), linear-gradient(to right, rgba(0, 0, 0, 0.8) 0%, transparent 100%)"
              }}
            />
          </div>

          {/* Hero Content */}
          <div className="relative z-20 max-w-7xl mx-auto px-6 md:px-16 w-full grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            {/* TEXT (Left Column) */}
            <div className="flex flex-col items-start text-left">
              <h1 className="font-display-lg text-display-lg text-white max-w-2xl mb-8 tracking-tight reveal-item">
                See every settlement discrepancy <span className="italic font-light">before</span> it becomes a problem.
              </h1>
              <p className="font-body-lg text-body-lg text-zinc-400 max-w-xl mb-12 reveal-item">
                FinSight reconciles orders, payments, and settlements automatically — and investigates discrepancies with AI-backed evidence.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-start items-center reveal-item">
                <button
                  onClick={() => isAuthenticated ? navigate("/dashboard") : navigate("/login")}
                  className="bg-white text-black font-body-md text-body-md px-8 py-4 min-w-[200px] text-center transition-all hover:bg-zinc-200"
                >
                  Get Started
                </button>
                <button
                  onClick={() => setDemoOpen(true)}
                  className="bg-black/50 border border-white text-white font-body-md text-body-md px-8 py-4 min-w-[200px] text-center transition-all hover:bg-white hover:text-black backdrop-blur-sm"
                >
                  Book a Demo
                </button>
              </div>
            </div>

            {/* DASHBOARD CARD (Right Column) */}
            <div className="hidden md:block reveal-item w-full relative z-10 pointer-events-none">
              <div className="relative w-full h-[450px] border border-[#262626] bg-[#0F0F0F]/80 backdrop-blur-md p-6 flex flex-col gap-6 shadow-[0_0_50px_rgba(255,255,255,0.1)] opacity-80 group">

                {/* Mock Dashboard Content */}
                <div className="flex justify-between items-center border-b border-[#262626] pb-4">
                  <div className="font-data-md text-xs text-zinc-400 uppercase tracking-wider">Live Reconciliation</div>
                  <div className="flex gap-2">
                    <div className="w-2 h-2 rounded-full bg-white animate-pulse"></div>
                    <span className="font-data-md text-xs text-white">System Active</span>
                  </div>
                </div>

                {/* Mini KPI Row */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 border border-[#262626] bg-[#141414]">
                    <div className="font-data-md text-xs text-zinc-400 mb-2">Unreconciled Amount</div>
                    <div className="font-data-md text-xl text-white">₹124,500.00</div>
                    <div className="font-data-md text-[10px] text-[#34d399] mt-2">Reconciliation Rate: 99.7%</div>
                  </div>
                  <div className="p-4 border border-[#262626] bg-[#141414]">
                    <div className="font-data-md text-xs text-zinc-400 mb-2">Open Exceptions</div>
                    <div className="font-data-md text-xl text-red-400">3</div>
                  </div>
                </div>

                {/* Small Bar Chart */}
                <div className="h-24 flex items-end justify-between gap-2 px-2 reveal-item">
                  <div className="w-1/6 bg-white/20 h-1/3"></div>
                  <div className="w-1/6 bg-[#34d399] h-1/2 animate-bar-grow group-hover:scale-y-110 transition-transform duration-700 ease-out"></div>
                  <div className="w-1/6 bg-white/40 h-full"></div>
                  <div className="w-1/6 bg-white/30 h-3/4"></div>
                  <div className="w-1/6 bg-white/20 h-1/4"></div>
                  <div className="w-1/6 bg-[#ef4444] h-2/3 animate-bar-shrink group-hover:scale-y-125 transition-transform duration-1000 ease-out"></div>
                </div>

                {/* Mock Table Rows */}
                <div className="flex flex-col gap-2">
                  <div className="flex justify-between items-center p-2 border-b border-[#262626]">
                    <span className="font-data-md text-xs text-zinc-400">EX-901</span>
                    <span className="font-data-md text-xs text-red-400 border border-red-500/50 px-2 py-0.5">HIGH</span>
                  </div>
                  <div className="flex justify-between items-center p-2 border-b border-[#262626]">
                    <span className="font-data-md text-xs text-zinc-400">EX-902</span>
                    <span className="font-data-md text-xs text-[#34d399] border border-[#34d399] px-2 py-0.5">MED</span>
                  </div>
                  <div className="flex justify-between items-center p-2">
                    <span className="font-data-md text-xs text-zinc-400">EX-903</span>
                    <span className="font-data-md text-xs text-[#34d399] border border-[#34d399] px-2 py-0.5">LOW</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* 3. WHAT WE DO SECTION */}
        <section className="relative w-full h-auto py-40 flex items-center justify-center overflow-hidden">
          {/* Video Background */}
          <div className="absolute inset-0 z-0 overflow-hidden">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="absolute inset-0 w-full h-full object-cover"
            >
              <source src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260418_115655_b4d9cd77-feed-43cd-a198-af78ebdf1f7a.mp4" type="video/mp4" />
            </video>
            {/* Gradient Overlay */}
            <div className="absolute inset-0 bg-gradient-to-b from-[#0A0A0A] from-10% via-[#0A0A0A]/40 via-50% to-[#0A0A0A] to-100%"></div>
          </div>

          {/* Section Content */}
          <div className="relative z-10 max-w-7xl mx-auto px-6 md:px-16 text-center flex flex-col items-center justify-center w-full">
            {/* Circular Logo Mark */}
            <div className="w-12 h-12 rounded-full bg-white mb-8 reveal-item transition-all duration-700 ease-out opacity-100 translate-y-0"></div>

            {/* Eyebrow */}
            <p className="font-label-caps text-label-caps text-zinc-400 mb-6 tracking-[0.2em] reveal-item">
              WHAT WE DO
            </p>

            {/* Headline */}
            <h2 className="font-display-lg text-5xl md:text-7xl lg:text-[80px] text-white font-bold mb-10 reveal-item">
              FinSight
            </h2>

            {/* Explainer */}
            <p className="font-body-md text-body-md md:text-body-lg text-zinc-400 max-w-[600px] mb-16 reveal-item">
              FinSight reconciles orders, payments, and settlements automatically, investigates every discrepancy with AI-backed evidence, and gives finance teams a clear, auditable trail from transaction to resolution.
            </p>

            {/* Capabilities Row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-16 max-w-5xl w-full mx-auto">
              <div className="text-center reveal-item">
                <h3 className="font-label-caps text-label-caps text-white mb-4 tracking-[0.1em]">RECONCILE</h3>
                <p className="font-body-md text-body-md text-zinc-400">Match every order, payment, and settlement automatically.</p>
              </div>
              <div className="text-center reveal-item">
                <h3 className="font-label-caps text-label-caps text-white mb-4 tracking-[0.1em]">INVESTIGATE</h3>
                <p className="font-body-md text-body-md text-zinc-400">AI-backed evidence for every discrepancy, with a rule-based fallback that never leaves one uninvestigated.</p>
              </div>
              <div className="text-center reveal-item">
                <h3 className="font-label-caps text-label-caps text-white mb-4 tracking-[0.1em]">RESOLVE</h3>
                <p className="font-body-md text-body-md text-zinc-400">A clear, auditable trail from detection to human sign-off.</p>
              </div>
            </div>
          </div>
        </section>

        {/* 4. FEATURE GRID ("Precision at Scale") */}
        <section id="features" className="relative py-32 bg-[#0A0A0A] overflow-hidden">
          <div className="relative z-10 max-w-7xl mx-auto px-6 md:px-16">
            <h2 className="font-display-lg text-5xl md:text-6xl text-white font-bold mb-16 text-center reveal-item">Precision at Scale</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 justify-center">
              {/* CARD 1 (Static Visual Anchor) */}
              <div className="border border-zinc-800 bg-[#141414] p-6 flex flex-col justify-between relative min-h-[460px] feature-card">
                <div className="flex-grow flex items-center justify-center py-6">
                  {/* Abstract diagram / dots */}
                  <div className="grid grid-cols-5 gap-2 opacity-50">
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/40 rounded-full"></div>
                    <div className="w-3 h-3 bg-[#34d399] rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/40 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/60 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-[#34d399] rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-[#34d399] rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/40 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                    <div className="w-3 h-3 bg-white/20 rounded-full"></div>
                  </div>
                </div>
                <div className="mt-auto pt-6 border-t border-zinc-800">
                  <h3 className="font-display-lg text-white">See it reconcile.</h3>
                </div>
              </div>

              {/* CARD 2 */}
              <div
                className={`border border-zinc-800 bg-[#141414] p-6 flex flex-col justify-between cursor-pointer accordion-card feature-card relative min-h-[460px] ${expandedCards[1] ? "expanded-parent" : ""}`}
                onClick={() => toggleAccordion(1)}
              >
                <div>
                  <div className="flex justify-between items-start mb-10">
                    <div className="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center">
                      <span className="material-symbols-outlined text-white text-sm">account_tree</span>
                    </div>
                    <span className="font-data-md text-white/50 text-xs">01</span>
                  </div>
                  <div className="mb-6 p-3 border border-zinc-800 bg-black/40">
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-label-caps text-[10px] text-white/40">MATCH RATE</span>
                      <span className="font-data-md text-xs text-[#34d399]">99.8%</span>
                    </div>
                    <div className="flex gap-1 h-1 items-end">
                      <div className="flex-grow bg-[#34d399] h-full"></div>
                      <div className="flex-grow bg-[#34d399] h-full"></div>
                      <div className="flex-grow bg-[#34d399] h-full"></div>
                      <div className="flex-grow bg-white/10 h-full"></div>
                    </div>
                  </div>
                </div>

                <div className="mt-auto">
                  <div className="flex justify-between items-center mb-6">
                    <h3 className="font-display-lg text-white min-w-0 flex-1 pr-3">Multi-Way Matching</h3>
                    <span className="w-7 h-7 rounded-full border border-white/30 flex items-center justify-center text-white/60 hover:text-white hover:border-white transition-colors flex-shrink-0 indicator">
                      <span className="material-symbols-outlined text-[16px]">add</span>
                    </span>
                  </div>

                  <div className={`accordion-content ${expandedCards[1] ? "expanded" : ""}`}>
                    <div className="accordion-inner flex flex-col gap-3">
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Reconciles across payment gateways</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Matches bank statements automatically</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Handles internal ledger entries</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Flags conditional rule exceptions</p>
                      </div>
                      <div className="stagger-item mt-4 border-t border-zinc-800 pt-4">
                        <a className="font-data-md text-xs text-white/60 hover:text-white flex items-center gap-1 transition-colors" href="#product">
                          Learn more <span className="material-symbols-outlined text-[10px] -rotate-45">arrow_forward</span>
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* CARD 3 */}
              <div
                className={`border border-zinc-800 bg-[#141414] p-6 flex flex-col justify-between cursor-pointer accordion-card feature-card relative min-h-[460px] ${expandedCards[2] ? "expanded-parent" : ""}`}
                onClick={() => toggleAccordion(2)}
              >
                <div>
                  <div className="flex justify-between items-start mb-10">
                    <div className="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center">
                      <span className="material-symbols-outlined text-white text-sm">troubleshoot</span>
                    </div>
                    <span className="font-data-md text-white/50 text-xs">02</span>
                  </div>
                  <div className="mb-6 p-3 border border-zinc-800 bg-black/40">
                    <div className="font-label-caps text-[10px] text-white/40 mb-2">RISK HEATMAP</div>
                    <div className="grid grid-cols-4 gap-1 w-16">
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/20"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/40"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                      <div className="w-3 h-3 bg-white/5"></div>
                    </div>
                  </div>
                </div>

                <div className="mt-auto">
                  <div className="flex justify-between items-center mb-6">
                    <h3 className="font-display-lg text-white min-w-0 flex-1 pr-3">Anomaly Detection</h3>
                    <span className="w-7 h-7 rounded-full border border-white/30 flex items-center justify-center text-white/60 hover:text-white hover:border-white transition-colors flex-shrink-0 indicator">
                      <span className="material-symbols-outlined text-[16px]">add</span>
                    </span>
                  </div>

                  <div className={`accordion-content ${expandedCards[2] ? "expanded" : ""}`}>
                    <div className="accordion-inner flex flex-col gap-3">
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Identifies structural mismatches</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Flags floating balances</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Predicts month-end variance</p>
                      </div>
                      <div className="stagger-item flex gap-3 items-start">
                        <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                        <p className="font-data-md text-sm text-white/80">Automated root-cause analysis</p>
                      </div>
                      <div className="stagger-item mt-4 border-t border-zinc-800 pt-4">
                        <a className="font-data-md text-xs text-white/60 hover:text-white flex items-center gap-1 transition-colors" href="#product">
                          Learn more <span className="material-symbols-outlined text-[10px] -rotate-45">arrow_forward</span>
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* CARD 4 */}
              <div
                className={`border border-zinc-800 bg-[#141414] p-6 flex flex-col justify-between cursor-pointer accordion-card feature-card relative min-h-[460px] ${expandedCards[3] ? "expanded-parent" : ""}`}
                onClick={() => toggleAccordion(3)}
              >
                <div>
                  <div className="flex justify-between items-start mb-10">
                    <div className="w-8 h-8 rounded-full border border-white/20 flex items-center justify-center">
                      <span className="material-symbols-outlined text-white text-sm">history_edu</span>
                    </div>
                    <span className="font-data-md text-white/50 text-xs">03</span>
                  </div>
                  <div className="mb-6 p-3 border border-zinc-800 bg-black/40">
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-label-caps text-[10px] text-white/40">BLOCK HASH</span>
                      <span className="text-[10px] text-[#34d399] flex items-center gap-1">
                        <span className="material-symbols-outlined text-[10px]">verified</span> VERIFIED
                      </span>
                    </div>
                    <div className="font-data-md text-[10px] text-white/60 truncate">0x7f8c2...3a2e9f</div>
                  </div>
                </div>

                <div className="mt-auto">
                  <div className="flex justify-between items-center mb-6">
                    <h3 className="font-display-lg text-white min-w-0 flex-1 pr-3">Immutable Audit</h3>
                    <span className="w-7 h-7 rounded-full border border-white/30 flex items-center justify-center text-white/60 hover:text-white hover:border-white transition-colors flex-shrink-0 indicator">
                      <span className="material-symbols-outlined text-[16px]">add</span>
                    </span>
                  </div>

                  <div className={`accordion-content ${expandedCards[3] ? "expanded" : ""}`}>
                  <div className="accordion-inner flex flex-col gap-3">
                    <div className="stagger-item flex gap-3 items-start">
                      <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                      <p className="font-data-md text-sm text-white/80">Cryptographically signed logs</p>
                    </div>
                    <div className="stagger-item flex gap-3 items-start">
                      <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                      <p className="font-data-md text-sm text-white/80">Full lineage of every adjustment</p>
                    </div>
                    <div className="stagger-item flex gap-3 items-start">
                      <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                      <p className="font-data-md text-sm text-white/80">Regulatory-ready reporting</p>
                    </div>
                    <div className="stagger-item flex gap-3 items-start">
                      <span className="material-symbols-outlined text-white/50 text-sm mt-0.5">check</span>
                      <p className="font-data-md text-sm text-white/80">Human-in-the-loop verification</p>
                    </div>
                    <div className="stagger-item mt-4 border-t border-zinc-800 pt-4">
                      <a className="font-data-md text-xs text-white/60 hover:text-white flex items-center gap-1 transition-colors" href="#product">
                        Learn more <span className="material-symbols-outlined text-[10px] -rotate-45">arrow_forward</span>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            </div>
          </div>
        </section>

        {/* 5. LIVE PRODUCT UI SECTION */}
        <section className="relative py-32 bg-[#0A0A0A] border-t border-zinc-850 overflow-hidden">
          {/* Video Background */}
          <div className="absolute inset-0 z-0 overflow-hidden">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="absolute inset-0 w-full h-full object-cover opacity-[0.7]"
              id="ui-video"
            />
            {/* Gradient Overlay */}
            <div className="absolute inset-0 bg-gradient-to-b from-[#0A0A0A] from-10% via-[#0A0A0A]/40 via-50% to-[#0A0A0A] to-100%"></div>
          </div>

          <div className="relative z-10 max-w-7xl mx-auto px-8 md:px-16">
            <div className="text-center mb-16 reveal-item">
              <h2 className="font-display-lg text-4xl md:text-5xl text-white font-bold mb-6">Built for the moment something doesn't add up.</h2>
              <p className="font-body-lg text-zinc-400 max-w-2xl mx-auto">High-density data views combined with targeted AI analysis to resolve edge cases instantly.</p>
            </div>

            <div className="border border-zinc-800 rounded-sm bg-[#0F0F0F] p-6 reveal-item">
              {/* Mock UI Header */}
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-zinc-800 pb-4 mb-6 gap-4">
                <div className="font-data-md text-zinc-400 uppercase tracking-wider">Exception Queue / EX-8924</div>
                <div className="flex gap-4">
                  <span className="font-data-md text-white bg-white/5 px-3 py-1 border border-zinc-800">Status: Open</span>
                  <span className="font-data-md text-white bg-white/5 px-3 py-1 border border-zinc-800">Severity: High</span>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Table Area */}
                <div className="lg:col-span-2 border border-zinc-800 overflow-x-auto">
                  <table className="w-full text-left border-collapse min-w-[500px]">
                    <thead>
                      <tr className="border-b border-zinc-800 bg-[#141414]">
                        <th className="p-3 font-data-md text-zinc-400 font-normal">Source</th>
                        <th className="p-3 font-data-md text-zinc-400 font-normal">ID</th>
                        <th className="p-3 font-data-md text-zinc-400 font-normal text-right">Amount</th>
                        <th className="p-3 font-data-md text-zinc-400 font-normal">Date</th>
                      </tr>
                    </thead>
                    <tbody className="font-data-md">
                      <tr className="border-b border-zinc-800 hover:bg-[#141414]">
                        <td className="p-3 text-white flex items-center gap-2">
                          Stripe Payment <span className="text-[#34d399] text-[10px] border border-[#34d399] px-1">Matched</span>
                        </td>
                        <td className="p-3 text-zinc-400">pi_3Mtw...</td>
                        <td className="p-3 text-white text-right">₹4,500.00</td>
                        <td className="p-3 text-zinc-400">Oct 24, 14:32</td>
                      </tr>
                      <tr className="border-b border-zinc-800 hover:bg-[#141414]">
                        <td className="p-3 text-white flex items-center gap-2">
                          Internal Order <span className="text-[#34d399] text-[10px] border border-[#34d399] px-1">Matched</span>
                        </td>
                        <td className="p-3 text-zinc-400">ORD-992</td>
                        <td className="p-3 text-white text-right">₹4,500.00</td>
                        <td className="p-3 text-zinc-400">Oct 24, 14:30</td>
                      </tr>
                      <tr className="border-b border-zinc-800 bg-red-900/10 hover:bg-red-900/20">
                        <td className="p-3 text-white">Bank Settlement</td>
                        <td className="p-3 text-zinc-400">SET-044</td>
                        <td className="p-3 text-red-400 text-right font-bold">₹4,350.00</td>
                        <td className="p-3 text-zinc-400">Oct 26, 09:15</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                {/* AI Panel */}
                <div className="border border-zinc-800 p-5 bg-[#111111]">
                  <div className="flex items-center gap-2 mb-4 pb-2 border-b border-zinc-800">
                    <span className="material-symbols-outlined text-white text-sm">auto_awesome</span>
                    <span className="font-label-caps text-white tracking-widest">AI ANALYSIS</span>
                  </div>
                  <p className="font-body-md text-zinc-400 mb-4 leading-relaxed">
                    Identified a ₹150.00 discrepancy. The bank settlement is lower than the initial payment. Analysis of the merchant agreement indicates a 3.3% cross-border processing fee was applied before disbursement.
                  </p>
                  <div className="font-data-md p-3 bg-black border border-zinc-800 text-zinc-400 mb-4">
                    Confidence: 98% (Rule: Cross-Border FX Fee)
                  </div>
                  <button
                    onClick={() => setDemoOpen(true)}
                    className="w-full bg-white text-black font-body-md py-2 hover:bg-zinc-200 transition-colors"
                  >
                    Approve Write-off
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* 6. ARCHITECTURE SECTION */}
        <section id="architecture" className="relative py-32 bg-[#0A0A0A] border-t border-zinc-850 overflow-hidden">
          {/* Video Background */}
          <div className="absolute inset-0 z-0 overflow-hidden">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="absolute inset-0 w-full h-full object-cover opacity-40"
              id="transition-video"
            />
            {/* Gradient Overlay */}
            <div className="absolute inset-0 bg-gradient-to-b from-[#0A0A0A] from-10% via-[#0A0A0A]/40 via-50% to-[#0A0A0A] to-100%"></div>
          </div>

          <div className="relative z-10 max-w-7xl mx-auto px-8 md:px-16">
            {/* Header */}
            <div className="text-center mb-16 reveal-item">
              <p className="font-data-md text-zinc-400 uppercase tracking-[0.2em] mb-4 text-sm">ARCHITECTURE</p>
              <h2 className="font-display-lg text-4xl md:text-5xl text-white font-bold mb-6">
                AI you can <span className="italic font-serif">verify</span>, not just trust.
              </h2>
              <p className="font-body-lg text-zinc-400 mb-8 max-w-3xl mx-auto">
                Every AI insight comes with a transparent breakdown of exactly which data points informed the conclusion. No black boxes. Just clear logic linked directly to your source data.
              </p>
              <ul className="flex flex-col md:flex-row justify-center items-center gap-6 font-body-md text-white">
                <li className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-sm">check_circle</span>
                  <span>Deterministic fallback rules</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-sm">check_circle</span>
                  <span>Full citation of source data</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-sm">check_circle</span>
                  <span>Human-in-the-loop approval</span>
                </li>
              </ul>
            </div>

            {/* Horizontal Diagram */}
            <div className="relative w-full border border-zinc-800 bg-[#0F0F0F] p-6 reveal-item delay-200 mb-32 flex flex-col md:flex-row items-center justify-between gap-6 opacity-100 translate-y-0 overflow-hidden">
              <div className="absolute inset-0 bg-[radial-gradient(rgba(255,255,255,0.1)_1px,transparent_1px)] bg-[size:16px_16px] opacity-50 z-0"></div>

              <div className="flex flex-col gap-3 relative z-10 w-full md:w-1/3">
                <div className="p-3 border border-zinc-800 bg-[#141414] flex justify-between items-center rounded-sm">
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-[14px] text-zinc-400">account_balance</span>
                    <span className="font-data-md text-xs text-white">Bank Feed</span>
                  </div>
                  <span className="font-data-md text-[10px] text-zinc-400">2.4k rows</span>
                </div>
                <div className="p-3 border border-zinc-800 bg-[#141414] flex justify-between items-center rounded-sm">
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-[14px] text-zinc-400">credit_card</span>
                    <span className="font-data-md text-xs text-white">Gateway</span>
                  </div>
                  <span className="font-data-md text-[10px] text-zinc-400">3.1k rows</span>
                </div>
                <div className="p-3 border border-zinc-800 bg-[#141414] flex justify-between items-center rounded-sm">
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-[14px] text-zinc-400">database</span>
                    <span className="font-data-md text-xs text-white">Ledger</span>
                  </div>
                  <span className="font-data-md text-[10px] text-zinc-400">2.9k rows</span>
                </div>
              </div>

              <div className="hidden md:block flex-grow h-[1px] bg-zinc-800 relative z-10"></div>

              <div className="p-4 border border-white bg-white text-black text-center shadow-[0_0_25px_rgba(255,255,255,0.4)] relative z-10 w-[140px] shrink-0 rounded-sm">
                <div className="font-headline-sm text-sm font-bold leading-tight">FinSight<br />Inference<br />Model</div>
              </div>

              <div className="hidden md:block flex-grow h-[1px] bg-zinc-800 relative z-10"></div>

              <div className="p-4 border border-zinc-800 bg-black z-10 flex flex-col gap-2 shadow-xl w-full md:w-1/3 rounded-sm">
                <div className="font-label-caps text-white text-[10px] tracking-widest border-b border-zinc-800 pb-2">OUTPUT INSIGHT</div>
                <div className="font-data-md text-[10px] text-zinc-400 flex flex-col gap-1 mt-1">
                  <div className="text-[#34d399] mb-1">✓ Amount matched</div>
                  <div className="opacity-80">→ via <span className="text-white">bank_statement.csv</span>:r44</div>
                  <div className="opacity-80">→ via <span className="text-white">stripe_export.csv</span>:r102</div>
                  <div className="opacity-80">→ via <span className="text-white">ledger.db</span>:id89</div>
                </div>
              </div>
            </div>

            {/* Exception Types / What We Catch */}
            <div id="what-we-catch" className="relative reveal-item w-full max-w-5xl mx-auto h-auto min-h-[500px] md:h-[720px] lg:h-[760px] flex flex-col md:block items-center justify-center my-12">
              {/* SVG Background for connecting lines */}
              <svg
                ref={svgRef}
                className="hidden md:block absolute inset-0 w-full h-full pointer-events-none z-0"
                id="catch-lines-svg"
                preserveAspectRatio="none"
                viewBox="0 0 1000 800"
              >
                <defs>
                  <filter id="glow">
                    <feGaussianBlur result="coloredBlur" stdDeviation="2"></feGaussianBlur>
                    <feMerge>
                      <feMergeNode in="coloredBlur"></feMergeNode>
                      <feMergeNode in="SourceGraphic"></feMergeNode>
                    </feMerge>
                  </filter>
                </defs>
                {/* Center hub ring outline around heading */}
                <circle
                  cx="500"
                  cy="400"
                  r="90"
                  stroke="white"
                  strokeWidth="1"
                  fill="none"
                  opacity="0.25"
                  filter="url(#glow)"
                />
                <g className="lines-group"></g>
              </svg>

              {/* Centered Heading in Cursive/Script Serif Italic inside Circle Ring */}
              <div
                className="hidden md:flex flex-col items-center justify-center absolute z-20 pointer-events-none rounded-full"
                style={{ left: "50%", top: "50%", transform: "translate(-50%, -50%)", width: "180px", height: "180px", textAlign: "center" }}
              >
                <h3 className="font-serif italic text-3xl lg:text-4xl text-white font-normal tracking-wide leading-[1.15] text-center drop-shadow-[0_4px_24px_rgba(0,0,0,1)] select-none">
                  What<br />We<br />Catch
                </h3>
              </div>

              {/* Mobile Heading */}
              <div className="block md:hidden text-center mb-6 w-full">
                <h3 className="font-serif italic text-4xl text-white font-normal">What We Catch</h3>
              </div>

              {/* Radial Cards Container */}
              <div className="relative w-full h-full md:absolute md:inset-0 grid grid-cols-1 sm:grid-cols-2 gap-3 md:block z-10">
                {/* Card 01 - Top (270°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "50%", top: "15%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[01]</span>
                    <span className="material-symbols-outlined text-white text-lg">scale</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Amount Mismatch
                  </h4>
                  <div className="terminus absolute left-1/2 bottom-0 -translate-x-1/2 translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 02 - Top-Right (315°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "77%", top: "24%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[02]</span>
                    <span className="material-symbols-outlined text-white text-lg">search</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Missing Payment
                  </h4>
                  <div className="terminus absolute left-0 bottom-0 -translate-x-1/2 translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 03 - Right (0°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "88%", top: "50%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[03]</span>
                    <span className="material-symbols-outlined text-white text-lg">content_copy</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Duplicate Entry
                  </h4>
                  <div className="terminus absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 04 - Bottom-Right (45°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "77%", top: "76%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[04]</span>
                    <span className="material-symbols-outlined text-white text-lg">schedule</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Timing Difference
                  </h4>
                  <div className="terminus absolute left-0 top-0 -translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 05 - Bottom (90°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "50%", top: "85%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[05]</span>
                    <span className="material-symbols-outlined text-white text-lg">percent</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Fee Discrepancy
                  </h4>
                  <div className="terminus absolute left-1/2 top-0 -translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 06 - Bottom-Left (135°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "23%", top: "76%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[06]</span>
                    <span className="material-symbols-outlined text-white text-lg">currency_exchange</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Currency FX Variance
                  </h4>
                  <div className="terminus absolute right-0 top-0 translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 07 - Left (180°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "12%", top: "50%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[07]</span>
                    <span className="material-symbols-outlined text-white text-lg">undo</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Refund Not Settled
                  </h4>
                  <div className="terminus absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>

                {/* Card 08 - Top-Left (225°) */}
                <div
                  className="catch-card p-3.5 hover:bg-white/5 transition-all flex flex-col gap-2 relative group bg-[#0A0A0A] border border-zinc-800 hover:border-zinc-500 rounded-md md:absolute md:w-[200px] lg:w-[215px] md:-translate-x-1/2 md:-translate-y-1/2 shadow-lg"
                  style={{ left: "23%", top: "24%" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-zinc-500 text-xs font-medium">[08]</span>
                    <span className="material-symbols-outlined text-white text-lg">backspace</span>
                  </div>
                  <h4 className="font-headline-sm text-xs md:text-sm font-semibold text-white tracking-tight leading-snug">
                    Chargeback Dispute
                  </h4>
                  <div className="terminus absolute right-0 bottom-0 translate-x-1/2 translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full opacity-0 group-hover:opacity-100 shadow-[0_0_8px_white] transition-opacity"></div>
                </div>
              </div>
            </div>
          </div>
        </section>

      </main>

      {/* 7. FOOTER */}
      <footer className="relative bg-[#050505] border-t border-zinc-800 py-16 overflow-hidden">
        <div className="absolute inset-0 z-0 bg-[radial-gradient(rgba(255,255,255,0.06)_2px,transparent_2px)] bg-[size:24px_24px]"></div>
        <div className="relative z-10 max-w-7xl mx-auto px-8 md:px-16 grid grid-cols-1 md:grid-cols-4 gap-12">
          <div className="col-span-1 md:col-span-2">
            <div className="font-headline-sm text-headline-sm font-bold text-white mb-4">FinSight</div>
            <p className="font-body-md text-zinc-400 max-w-sm mb-8">
              Automated reconciliation and AI-driven discrepancy resolution for modern finance teams.
            </p>
          </div>
          <div>
            <h4 className="font-label-caps text-white tracking-widest mb-6">PRODUCT</h4>
            <ul className="space-y-4 font-body-md text-zinc-400 text-sm">
              <li><a className="hover:text-white transition-colors" href="#product">Platform</a></li>
              <li><a className="hover:text-white transition-colors" href="#features">Solutions</a></li>
              <li><a className="hover:text-white transition-colors" href="#architecture">Security</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-label-caps text-white tracking-widest mb-6">COMPANY</h4>
            <ul className="space-y-4 font-body-md text-zinc-400 text-sm">
              <li><a className="hover:text-white transition-colors" href="#product">About Us</a></li>
              <li><a className="hover:text-white transition-colors" href="#product">Careers</a></li>
              <li><a className="hover:text-white transition-colors" href="#product">Contact</a></li>
            </ul>
          </div>
        </div>
        <div className="relative z-10 max-w-7xl mx-auto px-8 md:px-16 mt-16 pt-8 border-t border-zinc-800 flex flex-col md:flex-row justify-between items-center font-data-md text-sm text-zinc-400">
          <p>© 2026 FinSight Inc. All rights reserved.</p>
          <div className="flex gap-6 mt-4 md:mt-0">
            <a className="hover:text-white transition-colors" href="#product">Privacy Policy</a>
            <a className="hover:text-white transition-colors" href="#product">Terms of Service</a>
          </div>
        </div>
      </footer>

      {/* REQUEST DEMO MODAL */}
      <AnimatePresence>
        {demoOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Backdrop */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setDemoOpen(false)}
              className="absolute inset-0 bg-black/80 backdrop-blur-sm"
            />

            {/* Modal Dialog */}
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="relative w-full max-w-md bg-[#0f0f0f] border border-zinc-800 rounded-lg p-6 shadow-2xl z-10 overflow-hidden"
            >
              <div className="absolute top-0 right-0 p-4">
                <button
                  onClick={() => setDemoOpen(false)}
                  className="text-zinc-400 hover:text-white p-1 rounded-lg hover:bg-white/5 transition-all"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="flex items-center gap-2 mb-2">
                <Sparkles className="w-5 h-5 text-white" />
                <span className="text-xs font-semibold text-white uppercase tracking-wider">Request Access</span>
              </div>

              <h3 className="text-xl font-bold text-white mb-2">Experience FinSight</h3>
              <p className="text-sm text-zinc-400 mb-6">
                See how FinSight reconciles transactions and resolves discrepancies in real time.
              </p>

              {demoSubmitted ? (
                <div className="py-6 flex flex-col items-center text-center">
                  <div className="w-12 h-12 rounded-full bg-white/10 border border-white/20 flex items-center justify-center text-white mb-4">
                    <Check className="w-6 h-6" />
                  </div>
                  <h4 className="text-base font-semibold text-white mb-1">Demo Request Received</h4>
                  <p className="text-xs text-zinc-500">Checking slot availability...</p>
                </div>
              ) : (
                <form onSubmit={handleDemoSubmit} className="space-y-4">
                  <div>
                    <label className="text-xs font-semibold text-zinc-400 uppercase tracking-wider block mb-2">
                      Work Email
                    </label>
                    <input
                      type="email"
                      required
                      placeholder="you@company.com"
                      value={demoEmail}
                      onChange={(e) => setDemoEmail(e.target.value)}
                      className="w-full bg-black border border-zinc-800 rounded-none px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-white transition-colors"
                    />
                  </div>
                  <Button
                    type="submit"
                    className="w-full bg-white hover:bg-zinc-200 text-black font-semibold h-11 rounded-none transition-all duration-300"
                  >
                    Schedule Demo Session
                  </Button>
                </form>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  )
}
