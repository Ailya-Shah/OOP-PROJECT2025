
# 🌟 Remote Patient Monitoring System (RPMS)

*A comprehensive Java-based telemedicine platform with real-time monitoring, secure communication, and health analytics*

---

## 🚀 Key Features

### 👨‍⚕️ **Doctor Portal**
- **Real-time Patient Monitoring**: Track vital signs with threshold alerts
- **Secure Telemedicine**: Encrypted video consultations and messaging
- **e-Prescriptions**: Digital prescription management with reminders
- **Appointment Scheduling**: Calendar integration with automated reminders

### 🏥 **Patient Portal**
- **Health Data Upload**: CSV import for vital signs
- **Emergency Alerts**: One-touch panic button with SMS/email notifications
- **Medical History**: Centralized access to all health records
- **Trend Visualization**: Interactive charts for health metrics

### 👨‍💼 **Admin Console**
- **User Management**: CRUD operations for all user types
- **System Analytics**: Usage statistics and logs
- **Role-based Access**: Fine-grained permission control

---

## 🛠️ Technical Stack

| Component          | Technology                          |
|--------------------|-------------------------------------|
| **Core Framework** | Java 17, JavaFX                     |
| **Database**       | MySQL 8.0                          |
| **Security**       | Basic Auth (to be upgraded to JWT)  |
| **Notifications**  | Jakarta Mail, Twilio SMS            |
| **Data Parsing**   | OpenCSV                            |
| **Charts**         | JavaFX Charts                      |
| **Build Tool**     | Maven                              |

---

## 📦 Dependency Management

### Maven Configuration
```xml
<!-- Database -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Email -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>

<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>20</version>
</dependency>

<!-- Twilio (optional) -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.2.3</version>
</dependency>
```

### Manual JAR Downloads
For manual installation, download these required JARs:

1. **Jakarta Mail**
   - [jakarta.mail-2.0.1.jar](https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar)
   - [jakarta.activation-2.0.1.jar](https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar)

2. **MySQL Connector**
   - [mysql-connector-j-8.0.33.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar)

3. **Twilio**
   - [twilio-9.2.3.jar](https://repo1.maven.org/maven2/com/twilio/sdk/twilio/9.2.3/twilio-9.2.3.jar)

4. **JavaFX SDK**
   - [Download JavaFX 20](https://gluonhq.com/products/javafx/)

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- Java 17+ ([Download](https://adoptium.net/))
- MySQL 8.0+ ([Download](https://dev.mysql.com/downloads/))
- Maven 3.8+ ([Download](https://maven.apache.org/download.cgi))

### 2. Database Setup
```bash
# Create database
CREATE DATABASE hospitalmanagementsystem;

# Import schema
mysql -u root -p hospitalmanagementsystem < sql/setup.sql

# Create following tables in MySQL by running queries:
SCHEMA:
CREATE DATABASE hospitalmanagementsystem;
USE hospitalmanagementsystem;

-- Users table: Stores common user information
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_info VARCHAR(20) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    role ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Patients table: Stores patient-specific information
CREATE TABLE patients (
    user_id VARCHAR(50) PRIMARY KEY,
    birth_date DATE NOT NULL,
    admission_date DATE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Doctors table: Stores doctor-specific information
CREATE TABLE doctors (
    user_id VARCHAR(50) PRIMARY KEY,
    joining_date DATE NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Administrators table: Stores administrator-specific information
CREATE TABLE administrators (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    joining_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Appointments table: Stores appointment details
CREATE TABLE appointments (
    appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_time DATETIME NOT NULL,
    patient_id VARCHAR(50) NOT NULL,
    doctor_id VARCHAR(50) NOT NULL,
    status ENUM('Pending', 'Approved', 'Canceled') NOT NULL DEFAULT 'Pending',
    FOREIGN KEY (patient_id) REFERENCES patients(user_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(user_id) ON DELETE CASCADE
);

-- Vitals table: Stores patient vital signs
CREATE TABLE vitals (
    vital_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id VARCHAR(50) NOT NULL,
    checkup_date DATE NOT NULL,
    heart_rate DOUBLE NOT NULL,
    blood_pressure DOUBLE NOT NULL,
    body_temperature DOUBLE NOT NULL,
    oxygen_level DOUBLE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id) ON DELETE CASCADE
);

-- Feedback table: Stores feedback from doctors to patients
CREATE TABLE feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id VARCHAR(50) NOT NULL,
    doctor_id VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    comments TEXT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(user_id) ON DELETE CASCADE
);

-- Prescriptions table: Stores prescription details
CREATE TABLE prescriptions (
    prescription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id VARCHAR(50) NOT NULL,
    medication VARCHAR(100) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    schedule VARCHAR(100) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id) ON DELETE CASCADE
);

-- Messages table: Stores chat messages between users
CREATE TABLE messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id VARCHAR(50) NOT NULL,
    receiver_id VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Video Consultations table: Stores video call details
CREATE TABLE video_consultations (
    call_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id VARCHAR(50) NOT NULL,
    patient_id VARCHAR(50) NOT NULL,
    scheduled_time DATETIME NOT NULL,
    meeting_link VARCHAR(255) NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES doctors(user_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id) ON DELETE CASCADE
);

-- System Logs table: Stores system activity logs
CREATE TABLE system_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action VARCHAR(255) NOT NULL,
    log_date DATE NOT NULL
);
```

### 3. Configuration
Create `.env` file:
```env
DB_URL=jdbc:mysql://localhost:3306/hospitalmanagementsystem
DB_USER=root
DB_PASS=yourpassword
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your@gmail.com
SMTP_PASS=your-app-password
```

### 4. Build & Run
```bash
git clone https://github.com/Ailya-Shah/OOP-PROJECT2025
cd OOP-PROJECT2025
mvn clean install
mvn javafx:run
```


## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| MySQL Connection Failed | Verify credentials and service status |
| Email Not Sending | Check SMTP settings and enable less secure apps |
| JavaFX Errors | Ensure correct Java version and module path |

---

## 📊 Database Schema

```mermaid
erDiagram
    USERS ||--o{ PATIENTS : "1:1"
    USERS ||--o{ DOCTORS : "1:1"
    USERS ||--o{ ADMINS : "1:1"
    PATIENTS ||--o{ VITALS : "1:N"
    PATIENTS ||--o{ APPOINTMENTS : "1:N"
    PATIENTS ||--o{ FEEDBACK : "1:N"
    PATIENTS ||--o{ PRESCRIPTIONS : "1:N"
    DOCTORS ||--o{ APPOINTMENTS : "1:N"
```

---

## ✉️ Contact
Project Team:
- Ailya Zainab
- Luqman Shehzad  
- Muhammad Hassan

Repository: [https://github.com/Ailya-Shah/OOP-PROJECT2025](https://github.com/Ailya-Shah/OOP-PROJECT2025)

---

*"Innovating healthcare through secure technology"* 🚑💻
```
![Alt Text](https://github.com/Aliya-Shah/OOP-PROJECT2025/blob/main/image.png?raw=true)
