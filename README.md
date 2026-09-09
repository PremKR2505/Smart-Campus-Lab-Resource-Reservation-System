# Smart Campus Lab & Resource Reservation System

**Course:** CSE2006: Programming in Java  
**Institution:** Vellore Institute of Technology (VIT)  
**Project Category:** Academic Course Project  
**Author:** Rohan Chetty (Reg No: 23BCE10045)  
**Status:** Production Ready (v2.4.0-LTS) | 100% Test Suite Verification

---

## Project Overview

The **Smart Campus Lab & Resource Reservation System** is a robust, concurrent, object-oriented Java application engineered to eliminate double-booking, scheduling chaos, and facility contention across university engineering laboratories and research clusters.

Designed specifically around the **CSE2006 (Programming in Java)** curriculum, the system demonstrates industry-grade application of:
- **Object-Oriented Architecture:** Deep inheritance hierarchies, abstract base classes, interfaces, and dynamic runtime dispatch.
- **Multithreading & Concurrency Control:** Fair `ReentrantLock` slot managers and thread pools (`ExecutorService`, `Callable`, `Future`) providing zero double-bookings under concurrent race conditions.
- **Collections Framework:** Synchronized maps, concurrent maps (`ConcurrentHashMap`), sorted lists, and Java Streams API for analytics aggregation.
- **Data Persistence:** Thread-safe file-backed storage with atomic commit and CSV serialization preserving user profiles and booking ledgers.
- **Dual Presentation Layers:** An interactive ANSI terminal console and a modern Java Swing desktop GUI.

```
+-----------------------------------------------------------------------------+
|               SMART CAMPUS LAB & RESOURCE RESERVATION SYSTEM                |
|                    Vellore Institute of Technology (VIT)                    |
+-----------------------------------------------------------------------------+
               │                                            │
      ┌────────▼────────┐                          ┌────────▼────────┐
      │  Interactive    │                          │  Java Swing     │
      │  Terminal CLI   │                          │  Desktop GUI    │
      └────────┬────────┘                          └────────┬────────┘
               │                                            │
               └────────────────────┬───────────────────────┘
                                    │
                       ┌────────────▼────────────┐
                       │  Authentication & RBAC  │
                       │  (Student/Faculty/Admin)│
                       └────────────┬────────────┘
                                    │
                       ┌────────────▼────────────┐
                       │   Reservation Service   │
                       └────────────┬────────────┘
                                    │
                  ┌─────────────────┴─────────────────┐
                  │                                   │
         ┌────────▼────────┐                 ┌────────▼────────┐
         │ SlotLockManager │                 │ FileDataStorage │
         │ (ReentrantLock) │                 │ (data/*.csv)    │
         └─────────────────┘                 └─────────────────┘
```

---

## Key Features

1. **Role-Based Access Control (RBAC):**
   - **Students:** Browse facilities, check live slot availability, book slots up to 3 concurrent active reservations.
   - **Faculty:** Priority resource access, elevated quota limit of 10 concurrent slots.
   - **Lab Admins:** Maintenance mode toggle, resource decommissioning, conflict overrides, and audit logs.
2. **Campus Facility & Equipment Taxonomy:**
   - Pre-configured for VIT campus facilities:
     - `LAB-SCSE-101`: Advanced Computing Lab I (AB-1, 60 Workstations, Dual-Boot Linux/Win)
     - `LAB-AI-201`: NVIDIA Deep Learning & GPU Cluster (CCAC, 35 Workstations, CUDA 12)
     - `LAB-IOT-301`: IoT & Cyber-Physical Systems Lab (AB-2, 40 Workstations)
     - `LAB-ROBO-401`: Robotics & Automation Arena (Technology Tower, 30 Benches, ROS2)
     - `EQ-FPGA-01`: AMD Xilinx Artix-7 Development Kits
     - `EQ-DSO-02`: Keysight 200MHz Digital Storage Oscilloscopes
     - `EQ-VR-03`: Meta Quest 3 Spatial Testing Rigs
3. **Thread-Safe Concurrency Lock Engine (`SlotLockManager`):**
   - Employs a fine-grained `ConcurrentHashMap` of fair `ReentrantLock` instances keyed by `[resourceId#date#slotId]`.
   - Guaranteed zero double-bookings: when multiple threads fire simultaneous booking calls, exactly one succeeds and all others fail cleanly with descriptive conflict feedback.
4. **Deterministic Time-Slot Scheduling:**
   - 5 daily academic sessions (`SLOT-1` through `SLOT-5`), calculating availability in constant time.
5. **Real-Time Analytics & Reporting:**
   - Dynamic aggregation of facility utilization %, role-wise reservation distribution, top-demanded labs, and lock contention telemetry.
6. **Dual User Interfaces:**
   - Full keyboard-navigable terminal console with ANSI colors and formatted ASCII tables.
   - Java Swing desktop interface with split pane catalog, visual status badges, and booking modals.

---

## Technologies & Concepts Used

| Technology / Java Concept | Practical Implementation in Project |
| :--- | :--- |
| **Java Platform** | Java SE 17+ / Java 26 (Runtime verified) |
| **Object-Oriented Programming** | Abstract classes (`User`, `Resource`), inheritance (`Student`, `Faculty`, `LabAdmin`, `LabRoom`, `Equipment`), interfaces (`DataStorage`), encapsulation, and dynamic polymorphism. |
| **Multithreading & Concurrency** | `java.util.concurrent.locks.ReentrantLock`, `ConcurrentHashMap`, `ExecutorService`, `Callable<T>`, `Future<T>`, `AtomicLong`. |
| **Collections Framework** | `List`, `Map`, `Set`, `LinkedHashMap`, `ArrayList`, Java 8+ Streams and lambda expressions. |
| **File I/O & Persistence** | `BufferedReader`, `PrintWriter`, `FileWriter`, `Path`, atomic file serialization in `data/`. |
| **Desktop GUI** | Java Swing (`JFrame`, `JSplitPane`, `JTable`, `DefaultTableModel`, `JOptionPane`, `UIManager`). |
| **Quality Assurance** | Standalone multi-threaded validation and unit testing harness (`ReservationTestSuite`). |

---

## Directory Structure

```
Smart Campus Lab & Resource Reservation System/
├── .git/                                 # Initialized Git repository
├── .gitignore                            # Java-specific ignore file
├── build.bat                             # One-click Windows compilation script
├── run.bat                               # One-click execution script (CLI, GUI, Test)
├── statement.md                          # Problem statement & scope specification
├── README.md                             # Comprehensive GitHub repository guide
├── PROJECT_REPORT.md                     # 15-Section official academic submission report
├── scripts/
│   └── generate_report_pdf.py            # Automated PDF report generator
├── data/                                 # Persistent CSV data storage
│   ├── users.csv
│   ├── resources.csv
│   └── reservations.csv
├── bin/                                  # Compiled bytecode class files
└── src/
    └── com/
        └── campus/
            └── reserve/
                ├── Main.java             # Entry point (CLI, GUI, Test launcher)
                ├── config/
                │   └── CampusConfig.java # Campus settings & institutional presets
                ├── model/
                │   ├── User.java         # Base abstract user class
                │   ├── Student.java      # Student domain model
                │   ├── Faculty.java      # Faculty domain model
                │   ├── LabAdmin.java     # Lab Administrator domain model
                │   ├── UserRole.java     # RBAC roles enum
                │   ├── Resource.java     # Base abstract resource class
                │   ├── LabRoom.java      # Computing & lab facilities
                │   ├── Equipment.java    # Specialized portable hardware
                │   ├── ResourceStatus.java # Status lifecycle enum
                │   ├── TimeSlot.java     # Academic schedule slot definition
                │   ├── Reservation.java  # Booking transaction record
                │   └── ReservationStatus.java # Booking state enum
                ├── concurrency/
                │   ├── SlotLockManager.java       # ReentrantLock mutex registry
                │   ├── ReservationResult.java     # Transaction outcome telemetry
                │   └── ConcurrentBookingWorker.java # Multi-threaded stress task
                ├── storage/
                │   ├── DataStorage.java           # Storage interface
                │   └── FileDataStorage.java       # Atomic CSV file implementation
                ├── service/
                │   ├── AuthService.java           # Authentication & session service
                │   ├── ResourceService.java      # Facility catalog queries
                │   ├── ReservationService.java   # Concurrency-safe booking engine
                │   └── AnalyticsService.java     # Utilization & metrics service
                ├── ui/
                │   ├── ConsoleUI.java             # ANSI terminal interactive console
                │   └── CampusReserveGUI.java      # Modern Java Swing desktop window
                └── test/
                    └── ReservationTestSuite.java  # Automated unit & concurrency test suite
```

---

## Installation & Running Guide

### Prerequisites
- Java Development Kit (JDK 17 or higher, JDK 26 verified).
- Windows OS (PowerShell / Command Prompt) or macOS / Linux.

### 1. Quick Build
Run the included build script:
```powershell
.\build.bat
```
Or compile directly using `javac`:
```powershell
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" }; javac -d bin (Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
```

---

### 2. Running the Application

#### Mode A: Interactive Terminal CLI (Default)
```powershell
.\run.bat
# or directly:
java -cp bin com.campus.reserve.Main --cli
```

#### Mode B: Modern Desktop Swing GUI
```powershell
.\run.bat --gui
# or directly:
java -cp bin com.campus.reserve.Main --gui
```

#### Mode C: Automated Verification Test Suite
```powershell
.\run.bat --test
# or directly:
java -cp bin com.campus.reserve.Main --test
```

---

## Testing & Verification Instructions

The project comes with a built-in automated test suite (`ReservationTestSuite`) covering 4 critical test categories:
1. **OOP Polymorphism & Quota Enforcement:** Verifies student (3) vs faculty (10) quota calculation and administrative overrides.
2. **Resource Hierarchy & Technical Specs:** Verifies dynamic polymorphic dispatch between `LabRoom` and `Equipment`.
3. **Single-Threaded Transaction Lifecycle:** Tests booking confirmation, duplicate prevention, cancellation, and slot freeing.
4. **10-Thread Concurrency Race Condition Stress Test:** Fires 10 concurrent threads simultaneously trying to claim the exact same lab and slot. Asserts that **exactly 1 winner** succeeds and **9 requests are rejected cleanly** with zero deadlocks or data corruption.

Run tests at any time using:
```powershell
java -cp bin com.campus.reserve.Main --test
```

Expected Output:
```
================================================================
   ACADEMIC EVALUATION TEST SUITE: CSE2006 JAVA PROJECT          
   System: Smart Campus Lab & Resource Reservation System        
================================================================

>>> [1/4] Running OOP Polymorphism & Quota Tests...
  [PASS] Student Max Quota                             - Student quota must be 3
  [PASS] Faculty Max Quota                             - Faculty quota must be 10
  [PASS] Admin Override Check                          - Admin can override maintenance
  [PASS] Student Override Check                        - Student cannot override maintenance

>>> [2/4] Running Resource Hierarchy & Specs Tests...
  [PASS] LabRoom Polymorphic Type                      - LabRoom correctly identifies as Laboratory Room
  [PASS] Equipment Polymorphic Type                    - Equipment correctly identifies as Specialized Equipment
  [PASS] Lab GPU Detection                             - GPU capability verified

>>> [3/4] Running Single-Threaded Booking Lifecycle Tests...
  [PASS] Initial Booking Success                       - First booking should confirm
  [PASS] Duplicate Booking Prevention                  - Duplicate booking rejected: Double-booking prevented: Slot SLOT-1 is already confirmed by Test Runner
  [PASS] Reservation Cancellation                      - Cancellation should succeed and free slot
  [PASS] Re-booking Freed Slot                         - Slot can be booked after cancellation

>>> [4/4] Running Concurrency & Race-Condition Stress Test (10 Threads)...
    [WINNER] Competitor Student 2 secured reservation RES-20260915-B2DEBE
  [PASS] Exact Single Winner Under Race Condition      - Expected exactly 1 successful booking, got: 1
  [PASS] All Other Contenders Rejected Cleanly         - Expected 9 conflicts prevented, got: 9
  [PASS] Mutex Lock Registry Active                    - SlotLockManager recorded thread-safe acquisitions
    Concurrency Telemetry: 10 Locks Acquired, 0 Contention Collisions Prevented.

----------------------------------------------------------------
   TEST EXECUTION SUMMARY: 14 PASSED, 0 FAILED
----------------------------------------------------------------
   >>> ALL VERIFICATION GATES PASSED SUCCESSFULLY (100%) <<<
```

---

## Pre-Configured Test Accounts

| Role | Name | Identifier | Login Email / ID | Password | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Student** | Rohan Chetty | Reg: 23BCE10045 | `USR-STD-01` or `rohan.chetty@vitstudent.ac.in` | `vit@2024` | Quota: 3 Slots |
| **Student** | Ananya Sharma | Reg: 23BCE10210 | `USR-STD-02` or `ananya.sharma@vitstudent.ac.in` | `vit@2024` | Quota: 3 Slots |
| **Faculty** | Dr. Rajesh K. | Emp: EMP-5082 | `USR-FAC-01` or `rajesh.kumar@vit.ac.in` | `vit@fac` | Quota: 10 Slots |
| **Lab Admin** | Vikram Singh | Staff: ADM-004 | `USR-ADM-01` or `vikram.admin@vit.ac.in` | `vit@admin` | Maintenance Controls |

---

## License & Academic Attribution
This project was developed as an original academic course project for **CSE2006: Programming in Java** at **Vellore Institute of Technology (VIT)**. All rights reserved.
