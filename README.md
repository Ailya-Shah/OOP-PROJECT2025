

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

## 📦 Dependencies

All dependencies are managed through Maven. Key dependencies include:

```xml
<!-- Database -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.28</version>
</dependency>

<!-- Email -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>java.net.http</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>

<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version>
</dependency>

<!-- Twilio (optional) -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>8.31.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.8.2</version>
    <scope>test</scope>
</dependency>
```

Full `pom.xml` available in the project root.

---

## 🚀 Quick Start Guide

### 1. Prerequisites

- Java 17+ ([Download](https://adoptium.net/))
- MySQL 8.0+ ([Download](https://dev.mysql.com/downloads/))
- Maven 3.8+ ([Download](https://maven.apache.org/download.cgi))


Here are the direct download links for all required JAR files:

## 📥 Essential JAR File Downloads

### 1. **Jakarta Mail (Email Notifications)**
- [jakarta.mail-2.0.1.jar](https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar)
- [jakarta.activation-2.0.1.jar](https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar) *(Required dependency)*

### 2. **MySQL JDBC Connector**
- [mysql-connector-j-8.0.33.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar) *(Latest version)*
- [Alternative Download](https://dev.mysql.com/downloads/connector/j/) (Official MySQL site)

### 3. **Twilio (SMS Notifications)**
- [twilio-9.2.3.jar](https://repo1.maven.org/maven2/com/twilio/sdk/twilio/9.2.3/twilio-9.2.3.jar) *(Latest version)*
- [jwt-0.9.1.jar](https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt/0.9.1/jjwt-0.9.1.jar) *(Required dependency)*

### 4. **JavaFX (For UI)**
- [Full JavaFX SDK 20](https://gluonhq.com/products/javafx/) (Download appropriate version for your OS)

### 5. **Apache HTTP Client**
- [httpclient-4.5.13.jar](https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar)
- [httpcore-4.4.13.jar](https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar) *(Required dependency)*

### 6. **JSON Processing (For Twilio)**
- [jackson-databind-2.15.2.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar)
- [jackson-core-2.15.2.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar)


### 2. Database Setup

```bash
# Create database (run in MySQL shell)
CREATE DATABASE hospitalmanagementsystem;
USE hospitalmanagementsystem;

# Create tables (schema provided in /sql/setup.sql)
mysql -u root -p hospitalmanagementsystem < sql/setup.sql

# Insert sample data (optional)
mysql -u root -p hospitalmanagementsystem < sql/sample_data.sql
```

### 3. Configuration

Create `.env` file in project root:

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/hospitalmanagementsystem
DB_USER=root
DB_PASS=yourpassword

# Email (Gmail example)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your@gmail.com
SMTP_PASS=your-app-password  # Use app-specific password

# Twilio (optional)
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
```

### 4. Build & Run

```bash
# Clone repository
git clone https://github.com/Ailya-Shah/OOP-PROJECT2025
cd rpms

# Build with Maven
mvn clean install

# Run application
mvn javafx:run
```

For development in IDE:
1. Import as Maven project
2. Set VM options: `--module-path /path/to/javafx-sdk-17.0.2/lib --add-modules javafx.controls,javafx.fxml`
3. Run `RPMSMain.java`

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| MySQL connection failed | Verify credentials in `.env` and ensure MySQL service is running |
| Email sending fails | Enable "Less secure apps" in Gmail or use app-specific password |
| Twilio SMS not working | Verify account SID/auth token and phone number format (+E.164) |
| JavaFX not loading | Ensure correct module path and Java 17+ is used |

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
    DOCTORS ||--o{ FEEDBACK : "1:N"
    PATIENTS ||--o{ VIDEO_CONSULTATIONS : "1:N"
    DOCTORS ||--o{ VIDEO_CONSULTATIONS : "1:N"
    USERS ||--o{ MESSAGES : "1:N"}

---

## 🔐 Security Notes

- Passwords are currently stored in plain text (for demo purposes)
- Always use HTTPS in production
- Recommended enhancements:
  - Password hashing (bcrypt)
  - TLS for database connections
  - JWT for authentication

---

## ✉️ Contact

- Ailya Zainab 
- Luqman Shehzad 
- Muhammad Hassan 

Project Link: https://github.com/Ailya-Shah/OOP-PROJECT2025

---

*"Innovating healthcare through secure, accessible technology"* 🚑💻
