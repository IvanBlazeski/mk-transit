# 🚌 MK Transit — Smart Public Transport App for North Macedonia

<p align="center">
  <img src="screenshots/Welcome ekran.png" width="200"/>
  <img src="screenshots/Home ekran.png" width="200"/>
  <img src="screenshots/Map Ekran.png" width="200"/>
</p>

## 📱 Overview

**MK Transit** is a smart public transport Android application for North Macedonia, built with Kotlin and Jetpack Compose. It connects passengers with transport operators, enabling real-time bus tracking, digital ticket purchasing, and seamless communication.

> **Academic Project** — PMP Course, FIKT, Summer Semester 2026  
> **Student:** Ivan Blazeski

---

## ✨ Features

### 👤 Passengers
- Browse and search bus/minibus lines
- View line details, stops, and ratings
- Buy digital tickets (Single, Daily, Weekly)
- QR code ticket generation and display
- Save favorite lines
- Real-time GPS location on map (OpenStreetMap)
- Send messages to operators
- Rate and review bus lines
- Push notifications for announcements

### 🏢 Transport Operators
- Create and manage company profile
- Add/delete bus lines
- Driver Mode with GPS sharing (every 5 seconds)
- QR ticket validation
- Send announcements to passengers

---

## 🛠️ Tech Stack

| Technology | Usage |
|------------|-------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | UI framework |
| **Material 3** | Design system |
| **Firebase Auth** | Authentication (Email, Google, Facebook, Anonymous) |
| **Firebase Firestore** | Cloud database |
| **Firebase Messaging** | Push notifications |
| **Firebase Analytics** | App analytics |
| **Firebase Crashlytics** | Crash reporting |
| **Room** | Local database |
| **Hilt** | Dependency injection |
| **OSMDroid** | Maps (OpenStreetMap) |
| **ZXing** | QR code generation |
| **Coil** | Image loading |

---

## 🔐 Authentication

The app supports all 4 required authentication methods:
- 📧 **Email & Password**
- 🔵 **Google Sign-In**
- 🔷 **Facebook Login**
- 👻 **Anonymous (Guest)**

---

## 📁 Project Structure

```
app/src/main/java/mk/fikt/mktransit/
├── data/
│   └── local/
│       ├── db/          # Room database
│       ├── dao/         # Data access objects
│       └── entity/      # Room entities
├── di/                  # Hilt dependency injection
├── domain/
│   └── model/           # Data models
├── ui/
│   ├── navigation/      # NavGraph, NavRoutes
│   ├── screens/
│   │   ├── auth/        # Login, Register, Welcome
│   │   ├── home/        # Home, Map
│   │   ├── lines/       # Line Detail
│   │   ├── tickets/     # Purchase, QR, My Tickets
│   │   ├── operator/    # Dashboard, Driver Mode
│   │   ├── messages/    # Messages, Chat
│   │   └── profile/     # Profile
│   └── theme/           # Colors, Typography
└── viewmodel/           # ViewModels
```

---

## 📸 Screenshots

| Welcome | Home | Map |
|---------|------|-----|
| ![Welcome](screenshots/Welcome%20ekran.png) | ![Home](screenshots/Home%20ekran.png) | ![Map](screenshots/Map%20Ekran.png) |

| Line Detail | Buy Ticket | QR Ticket |
|-------------|------------|-----------|
| ![Line](screenshots/Line%20Detail.png) | ![Buy](screenshots/Buy%20Ticket.png) | ![QR](screenshots/QR%20Code%20for%20Your%20Ticket.png) |

| Operator Dashboard | Driver Mode | Profile |
|--------------------|-------------|---------|
| ![Operator](screenshots/Operator%20Dashboard.png) | ![Driver](screenshots/Driver%20Mode%20-%20Driving.png) | ![Profile](screenshots/Profile%20Ekran.png) |

---

## 🌍 Internationalization

The app supports two languages:
- 🇬🇧 **English** (default)
- 🇲🇰 **Macedonian** (`values-b+mk`)

All strings are stored in XML resource files. The language changes automatically based on the device system language.

---

## 🗄️ Local Storage (Room)

Three local Room entities:
- **TicketEntity** — purchased tickets with QR content
- **FavoriteEntity** — saved bus lines
- **CachedLineEntity** — offline line cache

---

## 🗺️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture:

```
UI (Compose Screens)
        ↕
   ViewModels
        ↕
   Repositories
    ↙         ↘
Firestore    Room DB
```

---

## 📲 Firebase Configuration

The app uses the following Firebase services:
- **Authentication** — 4 sign-in methods
- **Firestore** — collections: `lines`, `users`, `tickets`, `operators`, `messages`, `vehicleLocations`, `announcements`, `favorites`
- **Cloud Messaging** — push notifications
- **Analytics** — user behavior tracking
- **Crashlytics** — crash reporting

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11+
- Android SDK 26+

### Setup
1. Clone the repository:
```bash
git clone https://github.com/IvanBlazeski/mk-transit.git
```

2. Open in Android Studio

3. Add your `google-services.json` to the `app/` folder

4. Run the app on an emulator or physical device (API 26+)

---

## 📹 Screencasts

Video demos are available in the `/screencasts` folder:
- App walkthrough
- Authentication flows
- Ticket purchase and QR code
- Operator dashboard
- Driver mode with GPS
- Map with stops

---

## 📋 Requirements Checklist

| Requirement | Status |
|-------------|--------|
| Multiple screens | ✅ |
| Phone & tablet layouts | ✅ |
| Portrait & landscape | ✅ |
| Macedonian & English | ✅ |
| All strings in XML | ✅ |
| Room local storage | ✅ |
| Firebase Auth (all 4) | ✅ |
| Firebase Firestore | ✅ |
| Firebase Messaging | ✅ |
| Firebase Analytics | ✅ |
| GitHub repository | ✅ |
| Screenshots | ✅ |
| Screencasts | ✅ |
| GPS integration | ✅ |
| QR Code tickets | ✅ |

---

## 👨‍💻 Author

**Ivan Blazeski**  
Faculty of Information and Communication Technologies (FIKT)  
Ss. Cyril and Methodius University, Skopje

---

## 📄 License

This project is developed for academic purposes at FIKT, Summer Semester 2026.