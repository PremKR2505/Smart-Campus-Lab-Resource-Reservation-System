# Project Statement: Smart Campus Lab & Resource Reservation System

**Course:** CSE2006 – Programming in Java  
**Academic Institution:** Vellore Institute of Technology (VIT)  
**Evaluation:** VITyarthi - Build Your Own Project (Flipped Course Evaluation)

---

## 1. Problem Statement

Across modern university campuses like VIT, academic laboratories and high-value research equipment—such as High-Performance AI GPU Clusters, IoT & Embedded Systems Benches, and FPGA Development Kits—experience heavy demand from thousands of undergraduate and postgraduate students, researchers, and faculty members. 

Historically, campus resource allocation has relied on decentralized logbooks, ad-hoc email requests, or uncoordinated departmental schedules. This introduces severe systemic bottlenecks:
1. **Double-Booking & Scheduling Collisions:** Simultaneous uncoordinated booking attempts lead to conflicts where multiple batches arrive at the same facility.
2. **Under-Utilization & Unbalanced Peak Loads:** Premium resources (e.g., NVIDIA A100 GPU clusters, DSO oscilloscopes) sit idle during off-peak windows while being severely over-congested prior to deadlines.
3. **Absence of Concurrency Controls:** When reservation requests peak before mid-term or capstone submission deadlines, manual or naive single-threaded systems fail to enforce thread-safe mutual exclusion, causing race conditions, corrupted reservation logs, and ghost bookings.
4. **Lack of Role-Based Governance & Quota Enforcement:** Without automated quota monitoring, individual users can monopolize scarce slots, preventing equitable access for peers.

The **Smart Campus Lab & Resource Reservation System** resolves these challenges by providing an automated, high-throughput, concurrency-safe reservation engine with role-based policies, fine-grained thread synchronization, and real-time availability visibility across both command-line and graphical interfaces.

---

## 2. Scope of the Project

The system encompasses the complete reservation and facility lifecycle for university laboratories, workstations, and specialized hardware:

- **Campus Facility Cataloging:** Structured taxonomy of computing labs, robotics arenas, and portable scientific equipment across campus academic blocks (e.g., Academic Block 1, Academic Block 2, Technology Tower, and Central Computing Center).
- **Time-Slot Standardization:** Partitioning daily operating hours into deterministic academic windows (e.g., Morning Sessions, Afternoon Sessions, Evening Research Hours).
- **Concurrency & Transaction Safety:** Guaranteed thread-level mutual exclusion using reentrant slot locking to prevent race conditions during high-volume booking traffic.
- **Role-Based Policy Enforcement:** Automatic enforcement of differentiated access tiers and active booking quotas for Students (max 3 slots), Faculty (max 10 slots with priority rights), and Lab Administrators (administrative override and maintenance controls).
- **Data Persistence & Audit Logging:** Atomic file-based state serialization preserving user profiles, facility configurations, and active reservation ledgers across restarts.
- **Dual User Interaction Layer:** Rich ANSI-formatted terminal CLI and modern Java Swing desktop GUI.
- **Administrative Intelligence:** Facility utilization metrics, peak session monitoring, and real-time concurrency collision tracking.

---

## 3. Target Users

The platform serves three primary stakeholder categories:

| Target User Group | Profile & Needs | Primary System Privileges |
| :--- | :--- | :--- |
| **Students** | Enrolled undergraduate and postgraduate engineering students (e.g., SCSE, SCOPE, SENSE) needing lab slots for coursework, projects, and research. | Browse facilities, view live slot schedules, book available slots up to quota (3), cancel personal bookings. |
| **Faculty / Researchers** | Professors, research scholars, and project mentors requiring dedicated computing clusters or equipment for research or lab evaluations. | Elevated quota limit (10), priority allocation, extended multi-session bookings. |
| **Lab Administrators** | Technical lab assistants and campus facility managers responsible for physical space upkeep, safety, and operational readiness. | Toggle lab maintenance mode, decommission equipment, cancel conflicting bookings, inspect utilization analytics. |

---

## 4. High-Level Features

1. **Role-Based Authentication & Session Management:**
   - Pre-authenticated roles with distinct permissions, credentials, and institutional identifiers (Registration Number for students, Employee ID for faculty, Staff ID for admins).
2. **Campus Facility & Equipment Discovery:**
   - Real-time querying and filtering by academic block, facility type, floor level, capacity, and specialized capabilities (e.g., GPU cluster, dual-boot OS, FPGA kits).
3. **Deterministic Slot Availability Matrix:**
   - Instant schedule matrix rendering for any selected date, displaying slot status (`AVAILABLE`, `RESERVED`, or `MAINTENANCE`).
4. **Concurrency-Safe Transaction Engine (`SlotLockManager`):**
   - Fine-grained `ReentrantLock` coordination keyed by resource and slot ID, preventing race conditions and double-booking during concurrent load.
5. **Active Quota & Schedule Conflict Validation:**
   - Algorithmic checks preventing users from exceeding their active booking threshold or creating overlapping schedules for themselves.
6. **Self-Service Reservation Management:**
   - Instant booking confirmation and user-initiated cancellations with immediate slot reclamation.
7. **Institutional Analytics Dashboard:**
   - Real-time tracking of total bookings, facility utilization rates, top-requested facilities, role distribution, and concurrency telemetry.
8. **Dual Interfaces (CLI & Swing Desktop GUI):**
   - Full-featured keyboard-driven console interface and an intuitive Java Swing GUI with calendar grid and visual slot selectors.
