# Project Documentation: Collaborative Task Manager (CTM)

## 1. Objective
The primary objective of the Collaborative Task Manager (CTM) project is to provide a robust, digital platform for efficiently managing software development projects. The system aims to bridge the gap between Project Managers and Team Members by standardizing task assignment, progress tracking, and communication throughout the Software Development Life Cycle (SDLC). It replaces ad-hoc management methods with a centralized, role-based application that ensures accountability and real-time visibility into project health.

## 2. Purpose
The purpose of this system is to facilitate seamless collaboration and project oversight. Built using **Java Swing** for a responsive desktop interface and **Google Firebase Firestore** for real-time cloud data persistence, the application serves two distinct user groups:
*   **Project Managers (Admins):** To oversee the entire project scope, manage the team directory, assign tasks, and analyze project performance through visual reports.
*   **Team Members:** To organize their individual workflow, access task instructions, track deadlines, and submit completed work directly through the platform.

## 3. Screens and Their Purpose

The application consists of **11 primary screens** (excluding dialogs), each designed for a specific function:

### General Screens
1.  **IntroScreen**
    *   **Purpose:** Application entry point.
    *   **Use:** Displays a full-screen welcome animation to enhance the user experience before transitioning to the login portal.

2.  **LoginPage**
    *   **Purpose:** Authentication and Security.
    *   **Use:** Allows users to securely log in using email and password. It intelligently detects the user's role (Admin vs. Member) and routes them to the appropriate Dashboard.

### Dashboard Containers
3.  **AdminDashboard**
    *   **Purpose:** The main navigation hub for Project Managers.
    *   **Use:** Provides a persistent sidebar menu allowing Admins to switch between Home, Team, Tasks, Reports, and Settings.

4.  **UserDashboard**
    *   **Purpose:** The main navigation hub for Team Members.
    *   **Use:** Provides a simplified sidebar restricted to personal views (Home, My Tasks, Notifications, Settings).

### Functional Screens
5.  **HomePage (Admin View)**
    *   **Purpose:** Project-level overview.
    *   **Use:** Displays high-level statistics (Pending, In Progress, Completed counts), a project progress bar broken down by SDLC phases, and "Quick Actions" for creating tasks or managing the team.

6.  **UserHomePage (Member View)**
    *   **Purpose:** Personal workload overview.
    *   **Use:** Focuses on the individual's performance. Displays personal task counters and a "My Activity Feed" showing the most recent tasks assigned to the logged-in user.

7.  **TaskScreen**
    *   **Purpose:** Centralized interface for task lifecycle management.
    *   **Use:**
        *   **Admins:** Can view *all* tasks, create/edit/delete tasks, and assign them to specific members.
        *   **Members:** Can view only *their* assigned tasks, download instruction files, and use the "Upload Work" feature to submit deliverables.
        *   **Features:** Includes filtering (By Status/Owner) and color-coded status badges.

8.  **TeamMembersScreen**
    *   **Purpose:** Team directory and management.
    *   **Use:** Displays a grid of all team members with their roles and SDLC categories. Admins can use this screen to Add, Edit, or Remove members from the project.

9.  **ReportsPage**
    *   **Purpose:** Data visualization and analytics.
    *   **Use:** Provides visual insights into project health, including:
        *   **Pie Chart:** Distribution of tasks by status (To Do vs. Done).
        *   **Bar Chart:** Task breakdown by Priority (High/Medium/Low).

10. **SettingsScreen**
    *   **Purpose:** User preferences and account management.
    *   **Use:** Allows users to update their profile information, change their password, and toggle application preferences (e.g., viewing completed tasks).

11. **NotificationsScreen**
    *   **Purpose:** Alert center.
    *   **Use:** Lists important updates, such as new task assignments or system messages, ensuring users stay informed of recent activity.
