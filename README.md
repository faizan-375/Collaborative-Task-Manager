# ⚡ DevOrbit

**Collaborative Task Manager for Software Development Teams**

DevOrbit is a desktop-based task management application built with **Java Swing** and **Google Firebase Firestore**. It provides SDLC-aware task assignment, role-based dashboards, deadline-driven performance analytics, and real-time team collaboration — all in a polished, modern UI.

---

## ✨ Features

| Feature | Description |
|---|---|
| **Role-Based Dashboards** | Separate Admin and Member views with tailored navigation |
| **SDLC Task Management** | Assign tasks organized by development lifecycle phases |
| **Deadline & Performance** | Automatic performance deductions for overdue tasks |
| **File Exchange** | Admins attach instruction files; members upload submissions |
| **Team Directory** | Full member management with roles and SDLC categories |
| **Reports & Analytics** | Pie and bar chart visualizations for project health |
| **Notification System** | Real-time alerts for task assignments and warnings |
| **Settings & Profiles** | User preferences, password changes, and profile management |

---

## 🏗️ Architecture

```
DevOrbit/
├── src/
│   └── main/
│       ├── java/com/mycompany/projectscd/    # Application source code
│       │   ├── LoginPage.java                 # Entry point & authentication
│       │   ├── IntroScreen.java               # Splash screen animation
│       │   ├── IntroPanel.java                # Splash animation panel
│       │   ├── AdminDashboard.java            # Admin navigation hub
│       │   ├── UserDashboard.java             # Member navigation hub
│       │   ├── HomePage.java                  # Admin project overview
│       │   ├── UserHomePage.java              # Member personal overview
│       │   ├── TaskScreen.java                # Task lifecycle management
│       │   ├── TeamMembersScreen.java         # Team directory
│       │   ├── ReportsPage.java               # Charts & analytics
│       │   ├── NotificationsScreen.java       # Alert center
│       │   ├── SettingsScreen.java            # User preferences
│       │   ├── AddTaskDialog.java             # Task creation dialog
│       │   ├── AddMemberDialog.java           # Member creation dialog
│       │   ├── FirebaseManager.java           # Firebase initialization
│       │   ├── PerformanceManager.java        # Deadline-based scoring
│       │   ├── Theme.java                     # UI design system
│       │   ├── GlowingProgressBar.java        # Custom progress component
│       │   ├── Task.java                      # Task data model
│       │   ├── Member.java                    # Member data model
│       │   ├── Category.java                  # SDLC category model
│       │   └── Notification.java              # Notification data model
│       └── resources/
│           └── images/                        # Application image assets
│               ├── logo.jpg
│               └── logo_v2.png
├── docs/
│   └── Documentation.md                       # Detailed project documentation
├── pom.xml                                    # Maven build configuration
├── .gitignore                                 # Git ignore rules
└── README.md                                  # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK 21+)
- **Apache Maven 3.8+**
- **Firebase Project** with Firestore enabled

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/DevOrbit.git
   cd DevOrbit
   ```

2. **Configure Firebase:**
   - Download your Firebase service account key from the [Firebase Console](https://console.firebase.google.com/)
   - Save it as `key.json` in the project root directory
   - ⚠️ **Never commit `key.json` to version control** — it's in `.gitignore`

3. **Build the project:**
   ```bash
   mvn clean compile
   ```

4. **Run the application:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.projectscd.LoginPage"
   ```

---

## 🔧 Tech Stack

| Component | Technology |
|---|---|
| **Language** | Java 21 |
| **UI Framework** | Java Swing (custom components) |
| **Database** | Google Firebase Firestore |
| **Authentication** | Firebase Admin SDK |
| **Build Tool** | Apache Maven |
| **Design System** | Custom `Theme.java` with gradient panels, rounded components |

---

## 📋 User Roles

### Admin / Project Manager
- View project-wide dashboards and statistics
- Create, edit, and delete tasks
- Manage team members (add/edit/remove)
- Access reports with pie charts and bar charts
- Receive performance warning notifications

### Team Member
- View personal dashboard with activity feed
- See only assigned tasks
- Download instruction files from admins
- Upload completed work submissions
- Track personal performance score

---

## 📖 Documentation

For detailed screen-by-screen documentation, see [docs/Documentation.md](docs/Documentation.md).

---

## 📄 License

This project is for educational purposes. Contact the maintainers for licensing inquiries.
