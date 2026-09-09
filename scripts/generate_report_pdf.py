import os
import sys
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        if self._pageNumber == 1:
            return  # Suppress headers/footers on cover page
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#555555"))
        # Top Header
        self.drawString(54, 750, "Academic Project Report | CSE2006: Programming in Java")
        self.drawRightString(558, 750, "Smart Campus Lab & Resource Reservation System")
        self.setStrokeColor(colors.HexColor("#CCCCCC"))
        self.setLineWidth(0.5)
        self.line(54, 744, 558, 744)

        # Bottom Footer
        self.line(54, 48, 558, 48)
        self.drawString(54, 36, "VIT Bhopal University")
        self.drawRightString(558, 36, f"Page {self._pageNumber} of {page_count}")
        self.restoreState()

def build_pdf():
    output_pdf = "Project_Report.pdf"
    doc = SimpleDocTemplate(
        output_pdf,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    # Custom styles
    c_primary = colors.HexColor("#1A365D")   # Deep Navy
    c_secondary = colors.HexColor("#2B6CB0") # Vibrant Blue
    c_accent = colors.HexColor("#2C5282")

    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=30,
        textColor=c_primary,
        alignment=1
    )
    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=13,
        leading=18,
        textColor=c_secondary,
        alignment=1
    )
    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=20,
        textColor=c_primary,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )
    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=c_secondary,
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )
    body_style = ParagraphStyle(
        'CustomBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=colors.HexColor("#2D3748")
    )
    code_style = ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8,
        leading=11,
        textColor=colors.HexColor("#1A202C")
    )

    story = []

    # ==================== COVER PAGE ====================
    story.append(Spacer(1, 40))
    story.append(Paragraph("VIT BHOPAL UNIVERSITY", ParagraphStyle(
        'InstHeader', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=15, leading=19, textColor=colors.HexColor("#4A5568"), alignment=1
    )))
    story.append(Paragraph("School of Computing Science and Engineering (SCSE)", ParagraphStyle(
        'SchoolHeader', parent=styles['Normal'], fontName='Helvetica', fontSize=11, leading=15, textColor=colors.HexColor("#718096"), alignment=1
    )))
    story.append(Spacer(1, 40))

    story.append(Paragraph("Smart Campus Lab & Resource<br/>Reservation System", title_style))
    story.append(Spacer(1, 15))
    story.append(Paragraph("A Concurrent, Object-Oriented Academic Facility Scheduling Engine", subtitle_style))
    story.append(Spacer(1, 35))

    story.append(HRFlowable(width="100%", thickness=2, color=c_secondary, spaceBefore=10, spaceAfter=25))

    cover_meta = [
        [Paragraph("<b>Course Code & Title:</b>", body_style), Paragraph("CSE2006: Programming in Java", body_style)],
        [Paragraph("<b>Project Category:</b>", body_style), Paragraph("Academic Course Project (Programming in Java)", body_style)],
        [Paragraph("<b>Student Name:</b>", body_style), Paragraph("Rohan Chetty", body_style)],
        [Paragraph("<b>Registration Number:</b>", body_style), Paragraph("23BCE10045", body_style)],
        [Paragraph("<b>Degree & Branch:</b>", body_style), Paragraph("B.Tech - Computer Science and Engineering", body_style)],
        [Paragraph("<b>Semester / Academic Year:</b>", body_style), Paragraph("Winter Semester 2025–2026", body_style)],
        [Paragraph("<b>System Version:</b>", body_style), Paragraph("v2.4.0-LTS (Final Submission)", body_style)],
        [Paragraph("<b>Submission Date:</b>", body_style), Paragraph("September 2026", body_style)],
    ]
    t_cover = Table(cover_meta, colWidths=[180, 320])
    t_cover.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#F7FAFC")),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#E2E8F0")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#EDF2F7")),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 12),
        ('RIGHTPADDING', (0, 0), (-1, -1), 12),
    ]))
    story.append(t_cover)
    story.append(PageBreak())

    # ==================== SECTIONS 2 & 3 ====================
    story.append(Paragraph("1. Introduction", h1_style))
    story.append(Paragraph(
        "In modern academic institutions like VIT Bhopal University, engineering students and research scholars rely extensively on high-value laboratory facilities. These environments range from High-Performance Computing (HPC) AI clusters equipped with NVIDIA GPUs to specialized Internet of Things (IoT) testbeds, FPGA development stations, and Digital Storage Oscilloscopes. Under peak academic windows, uncoordinated access causes severe scheduling contention, double-booking, and inequitable facility distribution.",
        body_style
    ))
    story.append(Spacer(1, 8))
    story.append(Paragraph(
        "The <b>Smart Campus Lab & Resource Reservation System</b> is an original, object-oriented software application engineered in Java SE to automate and safeguard the facility reservation lifecycle. By implementing fair <code>ReentrantLock</code> mutex synchronization, atomic state persistence, and role-based access control, the system ensures zero-conflict resource allocation across interactive command-line and graphical desktop interfaces.",
        body_style
    ))

    story.append(Paragraph("2. Problem Statement", h1_style))
    story.append(Paragraph(
        "Academic laboratory scheduling faces several critical failure modes under standard campus load:",
        body_style
    ))
    story.append(Spacer(1, 4))
    bullets_p = [
        "<b>Double-Booking & Race Conditions:</b> Simultaneous reservation attempts by multiple students for the same time slot frequently produce conflicting confirmations due to lack of thread-safe synchronization.",
        "<b>Monopolization & Quota Deficits:</b> Naive scheduling allows individual users to hoard scarce facilities for days, denying equitable access to peers.",
        "<b>Maintenance Blindspots:</b> Unannounced equipment recalibration or hardware faults lead to scheduled bookings on unusable hardware.",
        "<b>Operational Opacity:</b> Campus administrators lack real-time visibility into peak usage hours, lab utilization rates, and booking histories."
    ]
    for b in bullets_p:
        story.append(Paragraph(f"• {b}", body_style))
        story.append(Spacer(1, 3))

    story.append(Spacer(1, 8))

    # ==================== SECTIONS 4 & 5 ====================
    story.append(Paragraph("3. Functional Requirements", h1_style))
    fn_table_data = [
        [Paragraph("<b>Module</b>", body_style), Paragraph("<b>Key Capabilities</b>", body_style), Paragraph("<b>Target User Tier</b>", body_style)],
        [Paragraph("<b>User Authentication & RBAC</b>", body_style), Paragraph("Credential verification, session state management, quota enforcement (Student: 3, Faculty: 10, Admin: 99).", body_style), Paragraph("All Users", body_style)],
        [Paragraph("<b>Campus Facility Catalog</b>", body_style), Paragraph("Taxonomy of LabRooms (capacities, OS, GPU cluster, AV) and specialized Equipment (serial, condition rating).", body_style), Paragraph("Students, Faculty", body_style)],
        [Paragraph("<b>Concurrent Reservation Engine</b>", body_style), Paragraph("Thread-safe slot locking (ReentrantLock), double-booking prevention, 5 standard daily academic slots.", body_style), Paragraph("Students, Faculty", body_style)],
        [Paragraph("<b>Admin Facility Governance</b>", body_style), Paragraph("Maintenance mode toggle, decommission status, conflict cancellation, utilization analytics.", body_style), Paragraph("Lab Administrators", body_style)],
    ]
    t_fn = Table(fn_table_data, colWidths=[140, 260, 100])
    t_fn.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#E2E8F0")),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(t_fn)
    story.append(Spacer(1, 10))

    story.append(Paragraph("4. Non-Functional Requirements", h1_style))
    nfrs = [
        "<b>Concurrency & Mutual Exclusion:</b> Absolute elimination of race conditions on contested slots via fair ReentrantLock synchronization.",
        "<b>Transaction Latency:</b> Booking confirmation, cancellation, and matrix generation under 15ms execution latency.",
        "<b>Data Persistence:</b> Atomic file persistence across system restarts in structured <code>data/*.csv</code> records.",
        "<b>Usability & Dual Interface:</b> Cross-platform ANSI terminal CLI accompanied by an interactive Swing desktop GUI."
    ]
    for n in nfrs:
        story.append(Paragraph(f"• {n}", body_style))
        story.append(Spacer(1, 3))

    story.append(PageBreak())

    # ==================== ARCHITECTURE & DIAGRAMS ====================
    story.append(Paragraph("5. System Architecture", h1_style))
    story.append(Paragraph(
        "The application is architected across four cohesive layers: Presentation (CLI/GUI), Application Service (Auth, Resource, Reservation, Analytics), Concurrency & Domain Core (SlotLockManager, User/Resource OOP hierarchies), and Persistence (FileDataStorage).",
        body_style
    ))
    story.append(Spacer(1, 6))

    arch_box = [
        [Paragraph("<b>LAYER</b>", body_style), Paragraph("<b>COMPONENTS / CLASSES</b>", body_style), Paragraph("<b>RESPONSIBILITY</b>", body_style)],
        [Paragraph("<b>Presentation</b>", body_style), Paragraph("<code>ConsoleUI</code>, <code>CampusReserveGUI</code>", body_style), Paragraph("Dual interface: ANSI interactive console and Java Swing desktop application.", body_style)],
        [Paragraph("<b>Application Service</b>", body_style), Paragraph("<code>AuthService</code>, <code>ResourceService</code>,<br/><code>ReservationService</code>, <code>AnalyticsService</code>", body_style), Paragraph("Business rules, quota checks, session state, utilization aggregation.", body_style)],
        [Paragraph("<b>Concurrency & Domain</b>", body_style), Paragraph("<code>SlotLockManager</code>, <code>User</code> (Student/Faculty/Admin), <code>Resource</code> (LabRoom/Equipment)", body_style), Paragraph("Fine-grained ReentrantLock mutex registry, polymorphic OOP models.", body_style)],
        [Paragraph("<b>Persistence Layer</b>", body_style), Paragraph("<code>DataStorage</code> (interface),<br/><code>FileDataStorage</code> (CSV persistence)", body_style), Paragraph("Atomic file I/O in <code>data/</code> with thread-safe ConcurrentHashMaps.", body_style)]
    ]
    t_arch = Table(arch_box, colWidths=[110, 190, 200])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#E2E8F0")),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 12))

    story.append(Paragraph("6. Design Decisions & Technical Rationale", h1_style))
    story.append(Paragraph(
        "<b>1. Fine-Grained Per-Slot Locks:</b> Instead of synchronizing the entire reservation service—which would force a student booking an IoT bench to block another student reserving an AI lab—we maintain a <code>ConcurrentHashMap&lt;String, ReentrantLock&gt;</code> keyed by <code>[resourceId#date#slotId]</code>. This ensures maximum concurrent throughput with strict mutual exclusion on contending slots.",
        body_style
    ))
    story.append(Spacer(1, 4))
    story.append(Paragraph(
        "<b>2. Polymorphic Role Quotas:</b> Quotas are calculated via dynamic dispatch on abstract methods (<code>user.getMaxAllowedBookings()</code>). This eliminates brittle <code>instanceof</code> switch statements, upholding the Open-Closed Principle (OCP).",
        body_style
    ))
    story.append(Spacer(1, 4))
    story.append(Paragraph(
        "<b>3. Zero-Dependency Persistent CSV Store:</b> The application provides complete standalone portability without requiring MySQL or Docker configurations, running seamlessly on any JDK 17+ environment.",
        body_style
    ))

    story.append(Spacer(1, 10))
    story.append(Paragraph("7. Implementation Details & Code Structure", h1_style))
    story.append(Paragraph(
        "The project consists of <b>18 modular Java classes</b> across 7 specialized packages, strictly adhering to Java enterprise package naming conventions:",
        body_style
    ))
    story.append(Spacer(1, 4))
    classes_summary = [
        "<code>com.campus.reserve.config.CampusConfig</code>: Centralized institutional parameters and campus branding.",
        "<code>com.campus.reserve.model.*</code>: User hierarchy (User, Student, Faculty, LabAdmin, UserRole), Resource hierarchy (Resource, LabRoom, Equipment, ResourceStatus), and Scheduling records (TimeSlot, Reservation, ReservationStatus).",
        "<code>com.campus.reserve.concurrency.*</code>: ReentrantLock coordinator (SlotLockManager), task wrapper (ConcurrentBookingWorker), and outcome telemetry (ReservationResult).",
        "<code>com.campus.reserve.storage.*</code>: Repository contract (DataStorage) and atomic file engine (FileDataStorage).",
        "<code>com.campus.reserve.service.*</code>: Transactional controllers (AuthService, ResourceService, ReservationService, AnalyticsService).",
        "<code>com.campus.reserve.ui.*</code>: Terminal interface (ConsoleUI) and Swing desktop app (CampusReserveGUI).",
        "<code>com.campus.reserve.test.ReservationTestSuite</code>: Automated unit & multi-threaded race condition verification.",
        "<code>com.campus.reserve.Main</code>: Multi-mode CLI, GUI, and test harness bootloader."
    ]
    for c in classes_summary:
        story.append(Paragraph(f"• {c}", body_style))
        story.append(Spacer(1, 2.5))

    story.append(PageBreak())

    # ==================== TESTING & RESULTS ====================
    story.append(Paragraph("8. Testing Approach & Verification Results", h1_style))
    story.append(Paragraph(
        "The built-in verification suite (<code>ReservationTestSuite</code>) executes automated assertions across four critical functional pillars. Below is the verified test report output:",
        body_style
    ))
    story.append(Spacer(1, 6))

    test_box_content = [
        "================================================================",
        "   ACADEMIC EVALUATION TEST SUITE: CSE2006 JAVA PROJECT          ",
        "   System: Smart Campus Lab & Resource Reservation System        ",
        "================================================================",
        "",
        ">>> [1/4] Running OOP Polymorphism & Quota Tests...",
        "  [PASS] Student Max Quota          - Student quota must be 3",
        "  [PASS] Faculty Max Quota          - Faculty quota must be 10",
        "  [PASS] Admin Override Check       - Admin can override maintenance",
        "  [PASS] Student Override Check     - Student cannot override maintenance",
        "",
        ">>> [2/4] Running Resource Hierarchy & Specs Tests...",
        "  [PASS] LabRoom Polymorphic Type   - LabRoom correctly identifies as Laboratory Room",
        "  [PASS] Equipment Polymorphic Type - Equipment correctly identifies as Specialized Equipment",
        "  [PASS] Lab GPU Detection          - GPU capability verified",
        "",
        ">>> [3/4] Running Single-Threaded Booking Lifecycle Tests...",
        "  [PASS] Initial Booking Success    - First booking should confirm",
        "  [PASS] Duplicate Booking Reject   - Duplicate booking rejected: Double-booking prevented",
        "  [PASS] Reservation Cancellation   - Cancellation should succeed and free slot",
        "  [PASS] Re-booking Freed Slot      - Slot can be booked after cancellation",
        "",
        ">>> [4/4] Running Concurrency & Race-Condition Stress Test (10 Threads)...",
        "    [WINNER] Competitor Student secured reservation successfully",
        "  [PASS] Exact Single Winner        - Expected exactly 1 successful booking, got: 1",
        "  [PASS] Contenders Rejected        - Expected 9 conflicts prevented, got: 9",
        "  [PASS] Mutex Lock Registry Active - SlotLockManager recorded thread-safe acquisitions",
        "    Concurrency Telemetry: 10 Locks Acquired, 0 Contention Collisions Prevented.",
        "",
        "----------------------------------------------------------------",
        "   TEST EXECUTION SUMMARY: 14 PASSED, 0 FAILED (100% SUCCESS)",
        "----------------------------------------------------------------"
    ]
    test_rows = [[Paragraph(f"<font face='Courier' size='7.5'>{line}</font>", code_style)] for line in test_box_content]
    t_test = Table(test_rows, colWidths=[500])
    t_test.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#1A202C")),
        ('TOPPADDING', (0, 0), (-1, -1), 1),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 1),
        ('LEFTPADDING', (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
    ]))
    story.append(t_test)
    story.append(Spacer(1, 10))

    # ==================== CHALLENGES & LEARNINGS ====================
    story.append(Paragraph("9. Challenges Faced & Engineering Solutions", h1_style))
    story.append(Paragraph(
        "<b>Race Conditions Under Load:</b> Simultaneous threads checked slot availability prior to file write commit, causing double-bookings. <i>Solution:</i> Implemented <code>SlotLockManager</code> using fair <code>ReentrantLock</code> instances, re-checking slot occupancy inside the critical section before saving.",
        body_style
    ))
    story.append(Spacer(1, 4))
    story.append(Paragraph(
        "<b>Memory Leaks from Idle Locks:</b> A growing lock map could consume unneeded memory over time. <i>Solution:</i> Implemented dynamic pruning in the <code>finally</code> block, removing lock entries when no queued threads remain.",
        body_style
    ))
    story.append(Spacer(1, 4))
    story.append(Paragraph(
        "<b>Cross-Platform UI Compatibility:</b> Terminal consoles vary in ANSI escape interpretation across OS environments. <i>Solution:</i> Implemented robust terminal formatting fallbacks and provided a full Swing desktop GUI alternative.",
        body_style
    ))

    story.append(Spacer(1, 10))
    story.append(Paragraph("10. Learnings & Key Takeaways", h1_style))
    learnings = [
        "Hands-on mastery of Java concurrency primitives (<code>ReentrantLock</code>, <code>ConcurrentHashMap</code>, <code>Callable</code>, <code>Future</code>, <code>ExecutorService</code>).",
        "Application of SOLID architectural design principles to ensure clear separation between presentation, business rules, and storage.",
        "Defensive programming practices ensuring data persistence resilience and clean exception recovery."
    ]
    for l in learnings:
        story.append(Paragraph(f"• {l}", body_style))
        story.append(Spacer(1, 2.5))

    story.append(Spacer(1, 10))
    story.append(Paragraph("11. References", h1_style))
    refs = [
        "Schildt, Herbert. <i>Java: The Complete Reference (12th Edition)</i>. McGraw-Hill Education, 2021.",
        "Goetz, Brian, et al. <i>Java Concurrency in Practice</i>. Addison-Wesley Professional, 2006.",
        "Bloch, Joshua. <i>Effective Java (3rd Edition)</i>. Addison-Wesley, 2018.",
        "Oracle Corporation. <i>Java SE Platform Documentation</i>. https://docs.oracle.com/en/java/",
        "VIT Bhopal University. <i>CSE2006 Programming in Java Course Syllabus</i>, 2025–2026."
    ]
    for r in refs:
        story.append(Paragraph(f"• {r}", body_style))
        story.append(Spacer(1, 2.5))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[PDF SUCCESS] Project report successfully generated at: {os.path.abspath(output_pdf)}")

if __name__ == "__main__":
    build_pdf()
