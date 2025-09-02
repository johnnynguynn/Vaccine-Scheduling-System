# 💉 Vaccine Scheduling System

The **Vaccine Scheduling System** is a Java-based command-line application designed to simulate the scheduling, reservation, and management of COVID-19 vaccine appointments.  
It allows both **patients** and **caregivers** to interact with the system, providing features like account creation, login/logout, appointment scheduling, vaccine dose management, and cancellation.

---

## 🚀 Features

### 👤 User Management
- `create_patient <username> <password>` → Create a patient account with strong password enforcement through hash and salt.  
- `create_caregiver <username> <password>` → Create a caregiver account.  
- `login_patient <username> <password>` → Login as a patient.  
- `login_caregiver <username> <password>` → Login as a caregiver.  
- `logout` → Logout from the current session.

### 📅 Scheduling & Appointments
- `search_caregiver_schedule <date>` → Search for caregiver availability and available vaccines on a given date.  
- `reserve <date> <vaccine>` → Patients can reserve an appointment with an available caregiver.  
- `upload_availability <date>` → Caregivers can upload their availability for a date.  
- `cancel <appointment_id>` → Cancel an existing appointment (restores vaccine doses and caregiver availability).  
- `show_appointments` → View all scheduled appointments for the logged-in user.  

### 💉 Vaccine Management
- `add_doses <vaccine> <number>` → Caregivers can add or update available doses for a vaccine.  

### ❌ Exit
- `quit` → Exit the application.

---

## 🛠️ Tech Stack
- **Java** (Core application logic)  
- **SQLite** (Relational database for storing users, appointments, availability, and vaccines)  
- **JDBC** (Database connectivity)  

---
