# ComalApp

A mobile ordering application for university cafeterias built with Kotlin and Jetpack Compose. ComalApp connects students, kitchen workers, and administrators through a real-time order management system backed by Firebase.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features by Role](#features-by-role)
- [Data Models](#data-models)
- [Firebase Setup](#firebase-setup)
- [Cloud Functions Setup](#cloud-functions-setup)
- [Installation](#installation)
- [Customization](#customization)
- [Dependencies](#dependencies)
- [Navigation](#navigation)
- [Color Palette](#color-palette)

---

## Overview

ComalApp is a three-role cafeteria ordering system:

- **Students** browse the menu, place orders, track their status in real time, and receive push notifications when their order is ready.
- **Workers** manage incoming orders, update their status, toggle product availability, and confirm deliveries by scanning a QR code. They receive push notifications when a new order arrives.
- **Administrators** manage the full product catalog, user accounts, worker accounts, and monitor all orders.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Repository pattern |
| Authentication | Firebase Auth |
| Database | Cloud Firestore |
| File Storage | Firebase Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Cloud Functions | Firebase Functions v2 (Node.js) |
| Navigation | Navigation Compose |
| Image Loading | Coil |
| QR Scanning | ZXing Android Embedded |
| Dependency Injection | Manual (AppContainer) |

---

## Architecture

The project follows a clean MVVM architecture with a manual dependency injection container.

```
app/
├── data/
│   ├── model/          # Data classes
│   ├── repository/     # Repository layer (Firestore + Auth)
│   └── source/         # Firebase data sources
├── ui/
│   ├── components/     # Reusable Compose components
│   │   ├── admin/
│   │   ├── shared/
│   │   ├── student/
│   │   └── worker/
│   ├── navigation/     # NavGraph + AppDestinations
│   ├── screens/        # Screens organized by role
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── student/
│   │   └── worker/
│   ├── theme/          # Colors, typography, theme
│   └── viewmodel/      # ViewModels per screen
├── ComalApplication.kt # AppContainer + notification channel
├── ComalFirebaseMessagingService.kt # FCM message handling
└── MainActivity.kt     # Entry point + notification permission
functions/
├── index.js            # Cloud Functions (order triggers)
└── package.json
```

### Dependency Injection

`AppContainer` is instantiated once in `ComalApplication` and passed down through `LocalContext`. Each screen accesses repositories via `container.xRepository`.

### State Management

Each screen has a dedicated `UiState` data class collected as `StateFlow` via `collectAsStateWithLifecycle`. Shared state across a navigation graph (cart, notifications) is scoped to the graph's `NavBackStackEntry`.

---

## Features by Role

### Student

| Feature | Description |
|---|---|
| Registration & Login | Email/password with Firebase Auth |
| Forgot Password | Reset link sent to registered email |
| Change Password | Sends reset email from profile screen |
| Menu Browsing | Products filtered by category with real-time availability |
| Cart | Add, increment, decrement and remove items |
| Order Confirmation | Review screen before placing the order |
| Order Status | Real-time tracking with timeline (Received → Preparing → Ready → Delivered) |
| QR Ticket | Generated QR code per order for delivery confirmation |
| Order History | Full list of past orders with status |
| In-app Notifications | Real-time notification feed for every order status change |
| Push Notifications | System push notifications even when app is closed |
| Profile | Account info, order stats, change password, logout |

### Worker

| Feature | Description |
|---|---|
| Dashboard | Active order counts by status, urgent orders list, quick access cards |
| Orders List | Filterable list of active orders by status |
| Product Availability | Toggle products on/off for the current service |
| Order Detail | View items, advance order status, confirm delivery |
| QR Scanner | Scan student ticket QR to confirm delivery standalone |
| Push Notifications | Receives push notification when a new order arrives |

### Admin

| Feature | Description |
|---|---|
| Dashboard | Summary stats and quick access |
| Product Management | Create, edit, delete products with image upload |
| Order Management | View and manage all orders with full status control |
| Cancel Orders | Cancel any active order with confirmation dialog |
| User Management | View and delete student accounts |
| Worker Management | Create, view and delete worker accounts |
| Order Detail | Full control: advance, revert and cancel orders |

---

## Data Models

```kotlin
User(uid, email, fullName, expediente, role, fcmToken)

Category(id, name)

Product(id, name, description, price, available, imageUrl, categoryId)

Order(id, userId, status, total, productCount, qrCode, createdAt)

OrderItem(id, orderId, productId, quantity, subtotal)

Notification(id, userId, orderId, title, message, type, createdAt, read)
```

### Order Status Flow

```
pending → preparing → ready → delivered
                            ↘ cancelled (admin only)
```

Delivery from `ready` to `delivered` requires QR scan validation.

### Notification Types

| Type | Trigger | Color |
|---|---|---|
| `pending` | Order placed by student | Amber |
| `preparing` | Order moved to preparing | Blue |
| `ready` | Order ready for pickup | Green |
| `delivered` | Order delivered | Gray |
| `cancelled` | Order cancelled by admin | Red |

---

## Firebase Setup

### 1. Create a Firebase project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project on the **Blaze (pay as you go)** plan (required for Cloud Functions)
3. Register your Android app with package `com.example.comalapp`
4. Download `google-services.json` and place it in the `app/` directory

### 2. Enable Firebase Services

- **Authentication** → Sign-in method → Email/Password → Enable
- **Firestore Database** → Create database → Start in production mode
- **Storage** → Get started
- **Cloud Messaging** → No additional setup required (enabled by default)

### 3. Firestore Collections

| Collection | Description |
|---|---|
| `users` | All user accounts (students, workers, admins) |
| `categories` | Product categories |
| `products` | Menu products |
| `orders` | Customer orders |
| `orderItems` | Individual items per order |
| `notifications` | In-app notifications per user |

### 4. Firestore Indexes

The notifications query requires a composite index. On first launch, Firestore will log an error with a direct link to create it automatically. Click the link in Logcat and confirm in the Firebase Console.

Fields: `userId (Ascending)` + `createdAt (Descending)`

### 5. Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 6. Storage Security Rules

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### 7. Seed Initial Data

**Create an admin user:**
1. Firebase Console → Authentication → Add user (email + password)
2. Firestore → `users` → Add document with the user's UID as document ID:

```json
{
  "uid": "<firebase-auth-uid>",
  "email": "admin@comal.com",
  "fullName": "Administrator",
  "expediente": "",
  "role": "admin",
  "fcmToken": ""
}
```

**Create categories:**

Firestore → `categories` → Add documents:

```json
{ "name": "Bebidas" }
{ "name": "Comida" }
{ "name": "Snacks" }
```

**Add products with images:**

1. Firebase Console → Storage → Upload image to `products/{productId}.jpg`
2. Copy the **Download URL** (not the `gs://` path)
3. Firestore → `products` → Add document:

```json
{
  "name": "Café Espresso",
  "description": "Café negro doble shot",
  "price": 25.0,
  "available": true,
  "imageUrl": "<storage-download-url>",
  "categoryId": "<category-document-id>"
}
```

> The `categoryId` must match exactly the **document ID** of the category in the `categories` collection, not the category name.

---

## Cloud Functions Setup

Cloud Functions handle push notifications server-side, triggering automatically when orders are created or updated in Firestore.

### Prerequisites

- Node.js installed
- Firebase CLI: `npm install -g firebase-tools`

### Setup

```bash
# Login to Firebase
firebase login

# Initialize functions in project root
firebase init functions
# Select: Use existing project → JavaScript → No to ESLint → Yes to install dependencies

# Install dependencies inside functions folder
cd functions
npm install
cd ..

# Deploy functions
firebase deploy --only functions
```

### What the functions do

| Function | Trigger | Action |
|---|---|---|
| `onOrderCreated` | New document in `orders/` | Sends push to all workers |
| `onOrderStatusChanged` | Status field changes in `orders/` | Sends push to the student who placed the order |

### Verify deployment

Firebase Console → Functions → both functions should be listed and active.

---

## Installation

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 26+
- Physical Android device (push notifications do not work on emulators without Google Play)
- A Firebase project configured as described above

### Steps

```bash
# Clone the repository
git clone https://github.com/your-org/comalapp.git
cd comalapp

# Open in Android Studio
# File → Open → select the project folder

# Add google-services.json to app/
# Build → Make Project
# Run on physical device
```

### .gitignore

```
# Android
local.properties
*.iml
.gradle/
build/
app/build/

# Firebase Functions
functions/node_modules/
functions/.env
functions/.env.local
functions/.runtimeconfig.json

# Firebase CLI
.firebase/
.firebaserc
firebase-debug.log
firebase-debug.*.log
firestore-debug.log
ui-debug.log

# Firebase config (uncomment if repository is public)
# app/google-services.json

# Claude
skills-lock.json
```

---

## Customization

### App Icon

1. In Android Studio: **File → New → Image Asset**
2. Select **Icon Type**: Launcher Icons (Adaptive and Legacy)
3. Select **Source Asset**: Image → choose your PNG or SVG file
4. Adjust size and background color
5. Click **Next → Finish**

This replaces all files in `app/src/main/res/mipmap-*/` with correctly sized versions for every screen density.

### Theme Colors

Colors are defined in `ui/theme/Color.kt`. The main tokens used throughout the app:

| Token | Value | Usage |
|---|---|---|
| `primary` | `#16167A` | Main blue — top bars, buttons |
| `blueAccent` | `#2929A3` | Secondary blue |
| `blueHighlight` | `#06B6D4` | Cyan accent |
| `violet` | `#7C3AED` | Worker role accent |
| `pendient` | `#F59E0B` | Pending order status |
| `prep` | `#3B82F6` | Preparing order status |
| `ready` | `#16A34A` | Ready order status |
| `delivered` | `#9CA3AF` | Delivered order status |
| `danger` | `#D32F2F` | Errors and destructive actions |

---

## Dependencies

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.activity:activity-compose")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.lifecycle:lifecycle-runtime-compose")

// Firebase
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")

// Image loading
implementation("io.coil-kt:coil-compose:2.6.0")
implementation("io.coil-kt:coil-svg:2.6.0")

// QR scanning
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
implementation("com.google.zxing:core:3.5.3")
```

---

## Navigation

```
LOGIN
REGISTER
FORGOT_PASSWORD
│
├── STUDENT_GRAPH
│   ├── STUDENT_HOME
│   ├── STUDENT_MENU
│   ├── STUDENT_CART
│   ├── STUDENT_ORDER_CONFIRM
│   ├── STUDENT_ORDER_STATUS/{orderId}
│   ├── STUDENT_ORDER_HISTORY
│   ├── STUDENT_TICKET/{orderId}
│   ├── STUDENT_PROFILE
│   └── STUDENT_NOTIFICATIONS
│
├── WORKER_GRAPH
│   ├── WORKER_HOME
│   ├── WORKER_ORDERS
│   ├── WORKER_PRODUCTS
│   ├── WORKER_QR_SCANNER
│   └── WORKER_ORDER_DETAIL/{orderId}
│
└── ADMIN_GRAPH
    ├── ADMIN_DASHBOARD
    ├── ADMIN_PRODUCTS
    ├── ADMIN_PRODUCT_FORM?productId={productId}
    ├── ADMIN_ORDERS
    ├── ADMIN_ORDER_DETAIL/{orderId}
    ├── ADMIN_USERS
    ├── ADMIN_WORKERS
    └── ADMIN_WORKER_FORM
```

Session state is observed globally. If Firebase invalidates the auth token, the app automatically redirects to login from any screen. On app launch, if a valid session exists, the user is taken directly to their role's graph without passing through login.
