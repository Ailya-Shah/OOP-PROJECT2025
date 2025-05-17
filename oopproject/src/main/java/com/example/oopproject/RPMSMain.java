package com.example.oopproject;

import java.net.URLEncoder;
import java.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.File;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.NumberAxis;

/**
 * Interface defining operations for user management in the Remote Patient Monitoring System (RPMS).
 * Provides methods to retrieve user details such as ID, name, contact information, and role.
 */
interface UserOperations {
    /** @return The unique identifier of the user. */
    String getUserID();
    /** @return The name of the user. */
    String getName();
    /** @return The contact information of the user (e.g., phone number). */
    String getContactInfo();
    /** @return The gender of the user. */
    String getGender();
    /** @return The user's password. */
    String getPassword();
    /** @return The role of the user (e.g., PATIENT, DOCTOR, ADMIN). */
    String getRole();
    /** @return The email address of the user. */
    String getEmail();
}

/**
 * Abstract base class for all users in the RPMS.
 * Implements the {@link UserOperations} interface and provides common attributes and methods for user management.
 */
abstract class User implements UserOperations {
    private String userID;
    private String userName;
    private String contactInfo;
    private String password;
    private String gender;
    private String role;
    private String email;

    /**
     * Constructs a new User with the specified details.
     *
     * @param userID      The unique identifier for the user.
     * @param userName    The user's name.
     * @param contactInfo The user's contact information (e.g., phone number).
     * @param email       The user's email address.
     * @param password    The user's password.
     * @param gender      The user's gender.
     */
    public User(String userID, String userName, String contactInfo, String email, String password, String gender) {
        this.userID = userID;
        this.userName = userName;
        this.contactInfo = contactInfo;
        this.email = email;
        this.password = password;
        this.gender = gender;
    }

    /**
     * Sets the role of the user.
     *
     * @param role The role to assign (e.g., PATIENT, DOCTOR, ADMIN).
     */
    public void setRole(String role) { this.role = role; }

    /** @return The unique identifier of the user. */
    public String getUserID() { return userID; }

    /** @return The name of the user. */
    public String getName() { return userName; }

    /** @return The contact information of the user. */
    public String getContactInfo() { return contactInfo; }

    /** @return The email address of the user. */
    public String getEmail() { return email; }

    /** @return The user's password. */
    public String getPassword() { return password; }

    /** @return The gender of the user. */
    public String getGender() { return gender; }

    /** @return The role of the user. */
    public String getRole() { return role; }

    /**
     * Returns a string representation of the user.
     *
     * @return A formatted string containing user details.
     */
    @Override
    public String toString() {
        return String.format("User ID: %s\nUser Name: %s\nPhone Number: %s\nEmail: %s\nGender: %s",
                userID, userName, contactInfo, email, gender);
    }
}

/**
 * Represents a patient in the RPMS, extending the {@link User} class.
 * Provides patient-specific functionality such as viewing feedback, prescriptions, and requesting appointments.
 */
class Patient extends User {
    private LocalDate birthDate;
    private LocalDate admissionDate;
    private ChatClient chatClient;

    /**
     * Constructs a new Patient with the specified details.
     *
     * @param userID      The unique identifier for the patient.
     * @param name        The patient's name.
     * @param contactInfo The patient's contact information.
     * @param email       The patient's email address.
     * @param password    The patient's password.
     * @param gender      The patient's gender.
     * @param birthDate   The patient's date of birth.
     */
    public Patient(String userID, String name, String contactInfo, String email, String password, String gender, LocalDate birthDate) {
        super(userID, name, contactInfo, email, password, gender);
        this.birthDate = birthDate;
    }

    /** @return The patient's date of birth. */
    public LocalDate getBirthDate() { return birthDate; }

    /**
     * Sets the admission date of the patient.
     *
     * @param admissionDate The date of admission.
     */
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    /** @return The patient's admission date, or null if not set. */
    public LocalDate getAdmissionDate() { return admissionDate; }

    /**
     * Retrieves and displays feedback for the patient from the database.
     */
    public void viewFeedback() {
        DataBase db = DataBase.getInstance();
        List<Feedback> feedbacks = db.getFeedbacksForPatient(this.getUserID());
        for (Feedback f : feedbacks) {
            System.out.println("Feedback: " + f.getComments() + " (Date: " + f.getDate() + ")");
        }
    }

    /**
     * Retrieves and displays prescriptions for the patient from the database.
     */
    public void viewPrescriptions() {
        DataBase db = DataBase.getInstance();
        List<Prescription> prescriptions = db.getPrescriptionsForPatient(this.getUserID());
        for (Prescription p : prescriptions) {
            System.out.println(p);
        }
    }

    /**
     * Retrieves the patient's vital signs from the database.
     *
     * @return A list of {@link VitalSign} objects for the patient.
     */
    public List<VitalSign> getVitals() {
        DataBase db = DataBase.getInstance();
        return db.getVitalSigns(this.getUserID());
    }

    /**
     * Requests an appointment with a specified doctor at a given time.
     *
     * @param time   The date and time of the requested appointment.
     * @param doctor The doctor with whom the appointment is requested.
     */
    public void requestAppointment(LocalDateTime time, Doctor doctor) {
        DataBase db = DataBase.getInstance();
        Appointment appointment = new Appointment(0, time, this, doctor);
        appointment.setAppointmentStatus("Pending");
        db.addAppointment(appointment);
    }

    /** @return The patient's chat client instance. */
    public ChatClient getChatClient() { return chatClient; }

    /**
     * Sets the chat client for the patient.
     *
     * @param chatClient The chat client instance to set.
     */
    public void setChatClient(ChatClient chatClient) { this.chatClient = chatClient; }

    /**
     * Sends a chat message to another user.
     *
     * @param receiverID The ID of the message recipient.
     * @param message    The message content.
     */
    public void sendMessage(String receiverID, String message) {
        if (chatClient != null) chatClient.sendMessage(receiverID, message);
    }

    /**
     * Sends a real-time SMS to a doctor.
     *
     * @param doctor          The doctor to receive the SMS.
     * @param message         The message content.
     * @param smsNotification The SMS notification service.
     * @throws NotificationException If the SMS fails to send.
     */
    public void sendRealTimeSMS(Doctor doctor, String message, SMSNotification smsNotification) throws NotificationException {
        if (doctor != null && smsNotification != null) {
            smsNotification.sendNotification(message, doctor.getContactInfo());
        }
    }

    /**
     * Returns a string representation of the patient, including birth and admission dates.
     *
     * @return A formatted string with patient details.
     */
    @Override
    public String toString() {
        return super.toString() + String.format("\nBirth Date: %s\nAdmission Date: %s",
                birthDate, admissionDate != null ? admissionDate : "Not set");
    }
}

/**
 * Represents a doctor in the RPMS, extending the {@link User} class.
 * Provides doctor-specific functionality such as viewing patient vitals, prescribing medication, and managing appointments.
 */
class Doctor extends User {
    private LocalDate joiningDate;
    private String specialization;
    private ChatClient chatClient;

    /**
     * Constructs a new Doctor with the specified details.
     *
     * @param userID        The unique identifier for the doctor.
     * @param name          The doctor's name.
     * @param contactInfo   The doctor's contact information.
     * @param email         The doctor's email address.
     * @param password      The doctor's password.
     * @param gender        The doctor's gender.
     * @param specialization The doctor's medical specialization.
     * @param joiningDate   The date the doctor joined the system.
     */
    public Doctor(String userID, String name, String contactInfo, String email, String password, String gender, String specialization, LocalDate joiningDate) {
        super(userID, name, contactInfo, email, password, gender);
        this.joiningDate = joiningDate;
        this.specialization = specialization;
    }

    /**
     * Sets the joining date of the doctor.
     *
     * @param joiningDate The date the doctor joined.
     */
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    /** @return The date the doctor joined the system. */
    public LocalDate getJoiningDate() { return joiningDate; }

    /** @return The doctor's medical specialization. */
    public String getSpecialization() { return specialization; }

    /** @return The doctor's chat client instance. */
    public ChatClient getChatClient() { return chatClient; }

    /**
     * Sets the chat client for the doctor.
     *
     * @param chatClient The chat client instance to set.
     */
    public void setChatClient(ChatClient chatClient) { this.chatClient = chatClient; }

    /**
     * Retrieves and displays vital signs for a specified patient.
     *
     * @param patientID The ID of the patient whose vitals are to be viewed.
     */
    public void viewPatientVitals(String patientID) {
        DataBase db = DataBase.getInstance();
        List<VitalSign> vitals = db.getVitalSigns(patientID);
        if (vitals.isEmpty()) System.out.println("No vitals found for patient " + patientID);
        else vitals.forEach(System.out::println);
    }

    /**
     * Adds feedback for a patient to the database.
     *
     * @param feedback The feedback to provide.
     */
    public void provideFeedback(Feedback feedback) {
        DataBase db = DataBase.getInstance();
        db.addFeedback(feedback);
    }

    /**
     * Prescribes medication for a patient and adds it to the database.
     *
     * @param prescription The prescription to add.
     */
    public void prescribeMedication(Prescription prescription) {
        DataBase db = DataBase.getInstance();
        db.addPrescription(prescription);
    }

    /**
     * Manages an appointment by approving or canceling it.
     *
     * @param appointmentId The ID of the appointment to manage.
     * @param action        The action to perform ("approve" or "cancel").
     */
    public void manageAppointment(long appointmentId, String action) {
        DataBase db = DataBase.getInstance();
        if (action.equalsIgnoreCase("approve")) db.updateAppointmentStatus(appointmentId, "Approved");
        else if (action.equalsIgnoreCase("cancel")) db.updateAppointmentStatus(appointmentId, "Canceled");
    }

    /**
     * Receives an alert message and displays it.
     *
     * @param message The alert message to display.
     */
    public void receiveAlert(String message) {
        System.out.println("Dr. " + getName() + " received alert: " + message);
    }

    /**
     * Sends a chat message to another user.
     *
     * @param receiverID The ID of the message recipient.
     * @param message    The message content.
     */
    public void sendMessage(String receiverID, String message) {
        if (chatClient != null) chatClient.sendMessage(receiverID, message);
    }

    /**
     * Returns a string representation of the doctor, including joining date and specialization.
     *
     * @return A formatted string with doctor details.
     */
    @Override
    public String toString() {
        return super.toString() + String.format("\nJoining Date: %s\nSpecialization: %s",
                joiningDate, specialization);
    }
}

/**
 * Represents an administrator in the RPMS, extending the {@link User} class.
 * Provides administrative functionality such as managing users and viewing system logs.
 */
class Administrator extends User {
    private LocalDate joiningDate;

    /**
     * Constructs a new Administrator with the specified details.
     *
     * @param userID      The unique identifier for the administrator.
     * @param userName    The administrator's name.
     * @param contactInfo The administrator's contact information.
     * @param email       The administrator's email address.
     * @param password    The administrator's password.
     * @param gender      The administrator's gender.
     * @param joiningDate The date the administrator joined the system.
     */
    public Administrator(String userID, String userName, String contactInfo, String email, String password, String gender, LocalDate joiningDate) {
        super(userID, userName, contactInfo, email, password, gender);
        this.joiningDate = joiningDate;
    }

    /**
     * Sets the joining date of the administrator.
     *
     * @param joiningDate The date the administrator joined.
     */
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    /** @return The date the administrator joined the system. */
    public LocalDate getJoiningDate() { return joiningDate; }

    /**
     * Returns a string representation of the administrator, including joining date.
     *
     * @return A formatted string with administrator details.
     */
    @Override
    public String toString() {
        return super.toString() + String.format("\nJoining Date: %s", joiningDate);
    }
}

/**
 * Represents an appointment in the RPMS.
 * Stores details such as appointment time, patient, doctor, and status.
 */
class Appointment {
    private long appointmentID;
    private LocalDateTime appointmentTime;
    private Patient patient;
    private Doctor doctor;
    private String appointmentStatus;

    /**
     * Constructs a new Appointment with the specified details.
     *
     * @param appointmentID   The unique identifier for the appointment.
     * @param appointmentTime The date and time of the appointment.
     * @param patient         The patient associated with the appointment.
     * @param doctor          The doctor associated with the appointment.
     */
    public Appointment(long appointmentID, LocalDateTime appointmentTime, Patient patient, Doctor doctor) {
        this.appointmentID = appointmentID;
        this.appointmentTime = appointmentTime;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentStatus = "Pending";
    }

    /**
     * Sets the appointment ID.
     *
     * @param appointmentID The unique identifier for the appointment.
     */
    public void setAppointmentID(long appointmentID) { this.appointmentID = appointmentID; }

    /**
     * Sets the appointment time.
     *
     * @param appointmentTime The date and time of the appointment.
     */
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    /**
     * Sets the patient for the appointment.
     *
     * @param patient The patient associated with the appointment.
     */
    public void setPatient(Patient patient) { this.patient = patient; }

    /**
     * Sets the doctor for the appointment.
     *
     * @param doctor The doctor associated with the appointment.
     */
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    /**
     * Sets the status of the appointment.
     *
     * @param appointmentStatus The status of the appointment (e.g., Pending, Approved, Canceled).
     */
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }

    /** @return The unique identifier of the appointment. */
    public long getAppointmentID() { return appointmentID; }

    /** @return The date and time of the appointment. */
    public LocalDateTime getAppointmentTime() { return appointmentTime; }

    /** @return The patient associated with the appointment. */
    public Patient getPatient() { return patient; }

    /** @return The doctor associated with the appointment. */
    public Doctor getDoctor() { return doctor; }

    /** @return The status of the appointment. */
    public String getAppointmentStatus() { return appointmentStatus; }

    /**
     * Returns a string representation of the appointment.
     *
     * @return A formatted string with appointment details.
     */
    @Override
    public String toString() {
        return String.format("\nAppointment ID: %d\nTime: %s\nPatient: %s\nDoctor: %s\nStatus: %s",
                appointmentID, appointmentTime, patient.getName(), doctor.getName(), appointmentStatus);
    }
}

/**
 * Represents a patient's vital signs in the RPMS.
 * Stores health metrics such as heart rate, blood pressure, and oxygen level.
 */
class VitalSign {
    private String patientID;
    private double heartRate;
    private double bloodPressure;
    private double bodyTemperature;
    private double oxygenLevel;
    private LocalDate checkupDate;

    /**
     * Constructs a new VitalSign with the specified health metrics.
     *
     * @param patientID      The ID of the patient.
     * @param heartRate      The patient's heart rate (beats per minute).
     * @param bloodPressure  The patient's blood pressure (mmHg).
     * @param bodyTemperature The patient's body temperature (Celsius).
     * @param oxygenLevel    The patient's oxygen saturation level (%).
     * @param checkupDate    The date of the checkup.
     */
    public VitalSign(String patientID, double heartRate, double bloodPressure, double bodyTemperature, double oxygenLevel, LocalDate checkupDate) {
        this.patientID = patientID;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.bodyTemperature = bodyTemperature;
        this.oxygenLevel = oxygenLevel;
        this.checkupDate = checkupDate;
    }

    /** @return The ID of the patient. */
    public String getPatientID() { return patientID; }

    /** @return The patient's heart rate. */
    public double getHeartRate() { return heartRate; }

    /** @return The patient's blood pressure. */
    public double getBloodPressure() { return bloodPressure; }

    /** @return The patient's body temperature. */
    public double getBodyTemperature() { return bodyTemperature; }

    /** @return The patient's oxygen level. */
    public double getOxygenLevel() { return oxygenLevel; }

    /** @return The date of the checkup. */
    public LocalDate getCheckupDate() { return checkupDate; }

    /**
     * Returns a string representation of the vital signs.
     *
     * @return A formatted string with vital sign details.
     */
    @Override
    public String toString() {
        return String.format("Patient: %s\nHeart Rate: %.2f\nBlood Pressure: %.2f\nTemperature: %.2f\nOxygen Level: %.2f\nDate: %s\n",
                patientID, heartRate, bloodPressure, bodyTemperature, oxygenLevel, checkupDate);
    }
}

/**
 * Represents feedback provided by a doctor to a patient in the RPMS.
 */
class Feedback {
    private long feedbackID;
    private String patientID;
    private String doctorID;
    private LocalDate date;
    private String comments;

    /**
     * Constructs a new Feedback instance.
     *
     * @param feedbackID The unique identifier for the feedback.
     * @param patientID  The ID of the patient receiving the feedback.
     * @param doctorID   The ID of the doctor providing the feedback.
     * @param date       The date the feedback was given.
     * @param comments   The feedback comments.
     */
    public Feedback(long feedbackID, String patientID, String doctorID, LocalDate date, String comments) {
        this.feedbackID = feedbackID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.date = date;
        this.comments = comments;
    }

    /**
     * Sets the feedback ID.
     *
     * @param feedbackID The unique identifier for the feedback.
     */
    public void setFeedbackID(long feedbackID) { this.feedbackID = feedbackID; }

    /** @return The unique identifier of the feedback. */
    public long getFeedbackID() { return feedbackID; }

    /** @return The ID of the patient receiving the feedback. */
    public String getPatientID() { return patientID; }

    /** @return The ID of the doctor providing the feedback. */
    public String getDoctorID() { return doctorID; }

    /** @return The date the feedback was given. */
    public LocalDate getDate() { return date; }

    /** @return The feedback comments. */
    public String getComments() { return comments; }

    /**
     * Returns a string representation of the feedback.
     *
     * @return A formatted string with feedback details.
     */
    @Override
    public String toString() {
        return String.format("Feedback: %s (Date: %s)", comments, date);
    }
}

/**
 * Represents a prescription issued to a patient in the RPMS.
 */
class Prescription {
    private long prescriptionID;
    private String patientID;
    private String medication;
    private String dosage;
    private String schedule;

    /**
     * Constructs a new Prescription with the specified details.
     *
     * @param prescriptionID The unique identifier for the prescription.
     * @param patientID      The ID of the patient receiving the prescription.
     * @param medication     The prescribed medication.
     * @param dosage         The dosage instructions.
     * @param schedule       The schedule for taking the medication.
     */
    public Prescription(long prescriptionID, String patientID, String medication, String dosage, String schedule) {
        this.prescriptionID = prescriptionID;
        this.patientID = patientID;
        this.medication = medication;
        this.dosage = dosage;
        this.schedule = schedule;
    }

    /**
     * Sets the prescription ID.
     *
     * @param prescriptionID The unique identifier for the prescription.
     */
    public void setPrescriptionID(long prescriptionID) { this.prescriptionID = prescriptionID; }

    /** @return The unique identifier of the prescription. */
    public long getPrescriptionID() { return prescriptionID; }

    /** @return The ID of the patient receiving the prescription. */
    public String getPatientID() { return patientID; }

    /** @return The prescribed medication. */
    public String getMedication() { return medication; }

    /** @return The dosage instructions. */
    public String getDosage() { return dosage; }

    /** @return The schedule for taking the medication. */
    public String getSchedule() { return schedule; }

    /**
     * Returns a string representation of the prescription.
     *
     * @return A formatted string with prescription details.
     */
    @Override
    public String toString() {
        return String.format("Prescription ID: %d\nMedication: %s\nDosage: %s\nSchedule: %s",
                prescriptionID, medication, dosage, schedule);
    }
}

/**
 * Represents a chat server in the RPMS, handling message storage and retrieval.
 */
class ChatServer {
    private DataBase db;

    /**
     * Constructs a new ChatServer with the specified database instance.
     *
     * @param db The database instance for message storage.
     */
    public ChatServer(DataBase db) {
        this.db = db;
    }

    /**
     * Sends a message from one user to another and stores it in the database.
     *
     * @param senderID   The ID of the sender.
     * @param receiverID The ID of the receiver.
     * @param message    The message content.
     */
    public void sendMessage(String senderID, String receiverID, String message) {
        db.addMessage(senderID, receiverID, message);
    }

    /**
     * Retrieves messages between two users from the database.
     *
     * @param user1 The ID of the first user.
     * @param user2 The ID of the second user.
     * @return A list of message strings between the two users.
     */
    public List<String> getMessages(String user1, String user2) {
        return db.getMessagesBetween(user1, user2);
    }

    /**
     * Retrieves messages between two users, including timestamps.
     *
     * @param userId1 The ID of the first user.
     * @param userId2 The ID of the second user.
     * @return A list of formatted message strings with timestamps.
     */
    public List<String> getMessagesBetween(String userId1, String userId2) {
        List<String> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId1);
            stmt.setString(2, userId2);
            stmt.setString(3, userId2);
            stmt.setString(4, userId1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String senderId = rs.getString("sender_id");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                messages.add("[" + timestamp + "] " + senderId + ": " + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}

/**
 * Represents a chat client for a user in the RPMS, facilitating message sending and retrieval.
 */
class ChatClient {
    private User user;
    private ChatServer server;
    private DataBase db;

    /**
     * Constructs a new ChatClient for a user with the specified server.
     *
     * @param user   The user associated with this client.
     * @param server The chat server instance.
     */
    public ChatClient(User user, ChatServer server) {
        this.user = user;
        this.server = server;
    }

    /**
     * Constructs a new ChatClient with the specified database instance.
     *
     * @param db The database instance for message storage.
     */
    public ChatClient(DataBase db) {
        this.db = db;
    }

    /**
     * Sends a message to another user via the chat server.
     *
     * @param receiverID The ID of the message recipient.
     * @param message    The message content.
     */
    public void sendMessage(String receiverID, String message) {
        server.sendMessage(user.getUserID(), receiverID, message);
    }

    /**
     * Displays messages between the user and another user.
     *
     * @param otherUserID The ID of the other user.
     */
    public void displayMessages(String otherUserID) {
        List<String> messages = server.getMessages(user.getUserID(), otherUserID);
        if (messages.isEmpty()) System.out.println("No messages found.");
        else messages.forEach(System.out::println);
    }

    /**
     * Views the chat history with another user.
     *
     * @param otherUserID The ID of the other user.
     */
    public void viewChatHistory(String otherUserID) {
        displayMessages(otherUserID);
    }

    /**
     * Stops the chat client and performs any necessary cleanup.
     */
    public void stop() {
        // Placeholder for cleanup
    }
}

/**
 * Interface for video call services in the RPMS.
 * Defines methods for generating meeting links and starting video calls.
 */
interface VideoCallService {
    /** @return The generated meeting link for the video call. */
    String generateMeetingLink();

    /**
     * Starts the video call.
     *
     * @return The meeting link for the call.
     * @throws Exception If the meeting link is not set or the call fails to start.
     */
    String startCall() throws Exception;
}

/**
 * Represents a video call in the RPMS, implementing the {@link VideoCallService} interface.
 * Manages video consultation details and meeting links.
 */
class VideoCall implements VideoCallService {
    private long callId;
    private String doctorID;
    private String patientID;
    private LocalDateTime scheduledTime;
    private String meetingLink;

    /**
     * Constructs a new VideoCall with the specified details.
     *
     * @param callId        The unique identifier for the video call.
     * @param doctorID      The ID of the doctor.
     * @param patientID     The ID of the patient.
     * @param scheduledTime The scheduled time for the call.
     * @param meetingLink   The meeting link for the video call.
     */
    public VideoCall(long callId, String doctorID, String patientID, LocalDateTime scheduledTime, String meetingLink) {
        this.callId = callId;
        this.doctorID = doctorID;
        this.patientID = patientID;
        this.scheduledTime = scheduledTime;
        this.meetingLink = meetingLink;
    }

    /**
     * Sets the call ID.
     *
     * @param callId The unique identifier for the video call.
     */
    public void setCallId(long callId) { this.callId = callId; }

    /** @return The unique identifier of the video call. */
    public long getCallId() { return callId; }

    /** @return The ID of the doctor. */
    public String getDoctorID() { return doctorID; }

    /** @return The ID of the patient. */
    public String getPatientID() { return patientID; }

    /** @return The scheduled time for the call. */
    public LocalDateTime getScheduledTime() { return scheduledTime; }

    /** @return The meeting link for the video call. */
    public String getMeetingLink() { return meetingLink; }

    /**
     * Generates a meeting link for the video call.
     *
     * @return The meeting link.
     */
    public String generateMeetingLink() { return meetingLink; }

    /**
     * Starts the video call using the meeting link.
     *
     * @return The meeting link for the call.
     * @throws Exception If the meeting link is not set.
     */
    public String startCall() throws Exception {
        if (meetingLink == null) throw new Exception("Meeting link not set.");
        return meetingLink;
    }
}

/**
 * Interface for triggering alerts in the RPMS.
 * Defines a method for sending alert notifications.
 */
interface Alertable {
    /**
     * Triggers an alert with the specified message.
     *
     * @param message The alert message.
     * @throws NotificationException If the alert fails to send.
     */
    void triggerAlert(String message) throws NotificationException;
}

/**
 * Custom exception for notification-related errors in the RPMS.
 */
class NotificationException extends Exception {
    /**
     * Constructs a new NotificationException with the specified message.
     *
     * @param message The error message.
     */
    public NotificationException(String message) { super(message); }
}

/**
 * Interface for notification services in the RPMS.
 * Defines methods for sending notifications and checking credential requirements.
 */
interface Notifiable {
    /**
     * Sends a notification to the specified recipient.
     *
     * @param message   The notification message.
     * @param recipient The recipient's contact information (e.g., email or phone number).
     * @throws NotificationException If the notification fails to send.
     */
    void sendNotification(String message, String recipient) throws NotificationException;

    /**
     * Sends a notification using the specified credentials.
     *
     * @param message   The notification message.
     * @param recipient The recipient's contact information.
     * @param username  The username for authentication.
     * @param password  The password for authentication.
     * @throws NotificationException If the notification fails to send.
     */
    void sendNotification(String message, String recipient, String username, String password) throws NotificationException;

    /**
     * Checks if the notification service requires credentials.
     *
     * @return True if credentials are required, false otherwise.
     */
    boolean requiresCredentials();
}

/**
 * Implements email notification functionality in the RPMS.
 * Sends email notifications using Gmail SMTP.
 */
class EmailNotification implements Notifiable {
    private String smtpHost = "smtp.gmail.com";
    private String smtpPort = "587";
    private String smtpUsername = "vengeance4354@gmail.com";
    private String smtpPassword = "inqr xmsa hcts rvvt";

    /**
     * Constructs a new EmailNotification instance.
     * Initializes SMTP credentials and logs them for debugging.
     */
    public EmailNotification() {
        smtpUsername = smtpUsername.trim();
        smtpPassword = smtpPassword.trim();
        System.out.println("Using Username: " + smtpUsername);
        System.out.println("Using Password: " + smtpPassword);
    }

    /**
     * Sends an email notification to the specified recipient using default credentials.
     *
     * @param message   The email message.
     * @param recipient The recipient's email address.
     * @throws NotificationException If the email fails to send.
     */
    @Override
    public void sendNotification(String message, String recipient) throws NotificationException {
        sendNotification(message, recipient, smtpUsername, smtpPassword);
    }

    /**
     * Sends an email notification using the specified credentials.
     *
     * @param message   The email message.
     * @param recipient The recipient's email address.
     * @param username  The SMTP username.
     * @param password  The SMTP password.
     * @throws NotificationException If the email fails to send.
     */
    @Override
    public void sendNotification(String message, String recipient, String username, String password) throws NotificationException {
        if (recipient == null || recipient.isEmpty()) throw new NotificationException("Invalid recipient for email.");
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", smtpHost);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        session.setDebug(true);

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            mimeMessage.setSubject("RPMS Notification");
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
            System.out.println("Email sent to " + recipient);
        } catch (MessagingException e) {
            throw new NotificationException("Error sending email: " + e.getMessage());
        }
    }

    /**
     * Checks if the email notification service requires credentials.
     *
     * @return True, as credentials are required.
     */
    @Override
    public boolean requiresCredentials() { return true; }
}

/**
 * Implements SMS notification functionality in the RPMS.
 * Sends SMS notifications using the Twilio API.
 */
class SMSNotification implements Notifiable {
    private String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
    private String authToken = System.getenv("TWILIO_AUTH_TOKEN");
    private String messagingServiceSid = System.getenv("TWILIO_MESSAGING_SERVICE_SID");

    /**
     * Constructs a new SMSNotification instance.
     * Initializes Twilio credentials, falling back to hardcoded values if environment variables are not set.
     */
    public SMSNotification() {
        if (accountSid == null || authToken == null || messagingServiceSid == null) {
            accountSid = "AC82d442f2832410f001efbc0fd5553e9a";
            authToken = "c8873126bd96a750ff159c53dab75514";
            messagingServiceSid = "MG02aa4a66e20ae708c8af53e470fc484a";
        }
        accountSid = accountSid.replaceAll("^\"|\"$", "");
        authToken = authToken.replaceAll("^\"|\"$", "");
        messagingServiceSid = messagingServiceSid.replaceAll("^\"|\"$", "");
    }

    /**
     * Sends an SMS notification to the specified recipient.
     *
     * @param message   The SMS message.
     * @param recipient The recipient's phone number in E.164 format.
     * @throws NotificationException If the SMS fails to send or the phone number is invalid.
     */
    @Override
    public void sendNotification(String message, String recipient) throws NotificationException {
        if (recipient == null || !recipient.matches("^\\+\\d{10,}$")) {
            throw new NotificationException("Invalid phone number. Use E.164 format (e.g., +1234567890).");
        }
        try {
            sendTwilioSMS(recipient, message);
        } catch (Exception e) {
            throw new NotificationException("Error sending SMS: " + e.getMessage());
        }
    }

    /**
     * Sends an SMS notification using the specified credentials (not used for Twilio).
     *
     * @param message   The SMS message.
     * @param recipient The recipient's phone number.
     * @param username  The username (not used).
     * @param password  The password (not used).
     * @throws NotificationException If the SMS fails to send.
     */
    @Override
    public void sendNotification(String message, String recipient, String username, String password) throws NotificationException {
        sendNotification(message, recipient);
    }

    /**
     * Checks if the SMS notification service requires credentials.
     *
     * @return True, as credentials are required.
     */
    @Override
    public boolean requiresCredentials() { return true; }

    /**
     * Sends an SMS using the Twilio API.
     *
     * @param toNumber    The recipient's phone number.
     * @param messageBody The SMS message content.
     * @throws Exception If the SMS fails to send.
     */
    private void sendTwilioSMS(String toNumber, String messageBody) throws Exception {
        String encodedAccountSid = URLEncoder.encode(accountSid, StandardCharsets.UTF_8.toString());
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + encodedAccountSid + "/Messages.json";

        String auth = accountSid + ":" + authToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String encodedToNumber = URLEncoder.encode(toNumber, StandardCharsets.UTF_8.toString());
        String encodedMessagingServiceSid = URLEncoder.encode(messagingServiceSid, StandardCharsets.UTF_8.toString());
        String encodedMessageBody = URLEncoder.encode(messageBody, StandardCharsets.UTF_8.toString());
        String formData = "To=" + encodedToNumber + "&MessagingServiceSid=" + encodedMessagingServiceSid + "&Body=" + encodedMessageBody;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new Exception("Failed to send SMS. Status: " + response.statusCode());
        System.out.println("SMS sent to " + toNumber);
    }
}

/**
 * Manages alert notifications in the RPMS.
 * Sends alerts to multiple recipients using a specified notification service.
 */
class AlertService {
    private Notifiable notifier;
    private List<String> recipients;

    /**
     * Constructs a new AlertService with the specified notification service and recipients.
     *
     * @param notifier   The notification service to use.
     * @param recipients The list of recipient contact information.
     */
    public AlertService(Notifiable notifier, List<String> recipients) {
        this.notifier = notifier;
        this.recipients = recipients != null ? recipients : new ArrayList<>();
    }

    /**
     * Sends an alert to all recipients.
     *
     * @param message The alert message.
     * @throws NotificationException If the alert fails to send or the service is misconfigured.
     */
    public void sendAlert(String message) throws NotificationException {
        if (notifier == null || recipients.isEmpty()) throw new NotificationException("Notification service misconfigured.");
        for (String recipient : recipients) {
            notifier.sendNotification(message, recipient);
        }
    }
}

/**
 * Custom exception for vital sign threshold violations in the RPMS.
 */
class VitalThresholdException extends Exception {
    /**
     * Constructs a new VitalThresholdException with the specified message.
     *
     * @param message The error message.
     */
    public VitalThresholdException(String message) { super(message); }
}

/**
 * Abstract base class for emergency alerts in the RPMS.
 * Implements the {@link Alertable} interface and provides functionality for checking vital signs.
 */
abstract class EmergencyAlert implements Alertable {
    protected VitalSign vital;
    protected AlertService alertService;
    protected Patient patient;

    /**
     * Constructs a new EmergencyAlert for a patient with the specified vital signs and alert service.
     *
     * @param patient      The patient associated with the alert.
     * @param vital        The vital signs to monitor.
     * @param alertService The alert service to use for notifications.
     */
    public EmergencyAlert(Patient patient, VitalSign vital, AlertService alertService) {
        this.patient = patient;
        this.vital = vital;
        this.alertService = alertService;
    }

    /** @return The vital signs being monitored. */
    public VitalSign getVital() { return vital; }

    /** @return The patient associated with the alert. */
    public Patient getPatient() { return patient; }

    /**
     * Checks if the vital signs are within normal thresholds.
     *
     * @param vital The vital signs to check.
     * @return True if all vital signs are within normal range, false otherwise.
     */
    public static boolean isWithinThreshold(VitalSign vital) {
        if (vital == null) return true;
        return (vital.getHeartRate() >= 60 && vital.getHeartRate() <= 100) &&
                (vital.getBloodPressure() >= 90 && vital.getBloodPressure() <= 140) &&
                (vital.getBodyTemperature() >= 36.1 && vital.getBodyTemperature() <= 37.2) &&
                (vital.getOxygenLevel() >= 95);
    }

    /**
     * Checks the patient's vital signs and triggers an alert if they are abnormal.
     *
     * @throws VitalThresholdException If vital or patient information is missing.
     * @throws NotificationException If the alert fails to send.
     */
    public void checkVitals() throws VitalThresholdException, NotificationException {
        if (vital == null || patient == null) throw new VitalThresholdException("Vital or patient information missing.");
        if (!isWithinThreshold(vital)) {
            String message = "Alert! Patient " + patient.getUserID() + "'s vitals are abnormal: " +
                    "HR=" + vital.getHeartRate() + ", BP=" + vital.getBloodPressure() +
                    ", Temp=" + vital.getBodyTemperature() + ", O2=" + vital.getOxygenLevel();
            triggerAlert(message);
        }
    }
}

/**
 * Represents a vital sign alert in the RPMS, extending {@link EmergencyAlert}.
 * Triggers notifications when vital signs are abnormal.
 */
class VitalAlert extends EmergencyAlert {
    /**
     * Constructs a new VitalAlert for a patient with the specified vital signs and alert service.
     *
     * @param patient      The patient associated with the alert.
     * @param vital        The vital signs to monitor.
     * @param alertService The alert service to use for notifications.
     */
    public VitalAlert(Patient patient, VitalSign vital, AlertService alertService) {
        super(patient, vital, alertService);
    }

    /**
     * Triggers an alert with the specified message using the alert service.
     *
     * @param message The alert message.
     * @throws NotificationException If the alert fails to send.
     */
    @Override
    public void triggerAlert(String message) throws NotificationException {
        if (alertService != null) alertService.sendAlert(message);
    }
}

/**
 * Represents a panic button alert in the RPMS, implementing the {@link Alertable} interface.
 * Allows patients to send emergency alerts to doctors.
 */
class PanicButton implements Alertable {
    private Patient patient;
    private Doctor doctor;
    private AlertService notificationService;

    /**
     * Constructs a new PanicButton for a patient and doctor with the specified notification service.
     *
     * @param patient            The patient associated with the panic button.
     * @param doctor             The doctor to receive the alert.
     * @param notificationService The notification service to use.
     */
    public PanicButton(Patient patient, Doctor doctor, AlertService notificationService) {
        this.patient = patient;
        this.doctor = doctor;
        this.notificationService = notificationService;
    }

    /**
     * Triggers an emergency alert to the doctor.
     *
     * @param message The alert message.
     * @throws NotificationException If the alert fails to send.
     */
    @Override
    public void triggerAlert(String message) throws NotificationException {
        if (patient == null || doctor == null) throw new NotificationException("Patient or doctor information missing.");
        if (notificationService != null) {
            notificationService.sendAlert(message);
            doctor.receiveAlert(message);
        }
    }

    /**
     * Simulates pressing the panic button, sending an emergency alert.
     *
     * @throws NotificationException If the alert fails to send.
     */
    public void pressPanicButton() throws NotificationException {
        triggerAlert("Emergency! Patient " + patient.getUserID() + " needs immediate attention.");
    }
}

/**
 * Handles file operations in the RPMS, specifically parsing CSV files containing vital signs.
 */
class FileHandling {
    /**
     * Parses a CSV file containing vital signs data for a patient.
     *
     * @param filePath  The path to the CSV file.
     * @param patientID The ID of the patient.
     * @return A list of {@link VitalSign} objects parsed from the CSV file.
     */
    public List<VitalSign> parseCSV(String filePath, String patientID) {
        List<VitalSign> vitals = new ArrayList<>();
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("Error: CSV file path cannot be empty");
            return vitals;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header
            if (line == null) {
                System.out.println("Error: CSV file is empty");
                return vitals;
            }
            int rowNum = 1;
            while ((line = br.readLine()) != null) {
                rowNum++;
                String[] fields = line.split(",");
                if (fields.length != 4) {
                    System.out.println("Invalid row " + rowNum + ": " + line);
                    continue;
                }
                try {
                    double heartRate = Double.parseDouble(fields[0].trim());
                    double oxygenLevel = Double.parseDouble(fields[1].trim());
                    double bloodPressure = Double.parseDouble(fields[2].trim());
                    double temperature = Double.parseDouble(fields[3].trim());
                    VitalSign vital = new VitalSign(patientID, heartRate, bloodPressure, temperature, oxygenLevel, LocalDate.now());
                    vitals.add(vital);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid data in row " + rowNum + ": " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
            return new ArrayList<>();
        }
        return vitals;
    }
}

/**
 * Singleton class for database operations in the RPMS.
 * Manages connections to a MySQL database and provides methods for user, appointment, and data management.
 */
class DataBase {
    private static DataBase instance;
    private Connection connection;

    /**
     * Private constructor to initialize the database connection.
     */
    private DataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/hospitalmanagementsystem";
            String username = "root";
            String password = "America23";
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            connection = null;
        }
    }

    /**
     * Retrieves the singleton instance of the DataBase.
     *
     * @return The singleton DataBase instance.
     */
    public static DataBase getInstance() {
        if (instance == null) instance = new DataBase();
        return instance;
    }

    /**
     * Retrieves the database connection.
     *
     * @return The database connection object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Adds a patient to the database.
     *
     * @param patient The patient to add.
     */
    public void addPatient(Patient patient) {
        if (connection == null) return;
        try {
            String insertUserSql = "INSERT INTO users (user_id, name, contact_info, gender, role, email) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSql)) {
                userStmt.setString(1, patient.getUserID());
                userStmt.setString(2, patient.getName());
                userStmt.setString(3, patient.getContactInfo());
                userStmt.setString(4, patient.getGender());
                userStmt.setString(5, "PATIENT");
                userStmt.setString(6, patient.getEmail());
                userStmt.executeUpdate();
            }

            String insertPatientSql = "INSERT INTO patients (user_id, birth_date, admission_date, email, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement patientStmt = connection.prepareStatement(insertPatientSql)) {
                patientStmt.setString(1, patient.getUserID());
                patientStmt.setDate(2, java.sql.Date.valueOf(patient.getBirthDate()));
                patientStmt.setDate(3, patient.getAdmissionDate() != null ? java.sql.Date.valueOf(patient.getAdmissionDate()) : null);
                patientStmt.setString(4, patient.getEmail());
                patientStmt.setString(5, patient.getPassword());
                patientStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding patient: " + e.getMessage());
        }
    }

    /**
     * Adds a doctor to the database.
     *
     * @param doctor The doctor to add.
     */
    public void addDoctor(Doctor doctor) {
        if (connection == null) return;
        try {
            String insertUserSql = "INSERT INTO users (user_id, name, contact_info, gender, role, email) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSql)) {
                userStmt.setString(1, doctor.getUserID());
                userStmt.setString(2, doctor.getName());
                userStmt.setString(3, doctor.getContactInfo());
                userStmt.setString(4, doctor.getGender());
                userStmt.setString(5, "DOCTOR");
                userStmt.setString(6, doctor.getEmail());
                userStmt.executeUpdate();
            }

            String insertDoctorSql = "INSERT INTO doctors (user_id, joining_date, specialization, email, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement doctorStmt = connection.prepareStatement(insertDoctorSql)) {
                doctorStmt.setString(1, doctor.getUserID());
                doctorStmt.setDate(2, java.sql.Date.valueOf(doctor.getJoiningDate()));
                doctorStmt.setString(3, doctor.getSpecialization());
                doctorStmt.setString(4, doctor.getEmail());
                doctorStmt.setString(5, doctor.getPassword());
                doctorStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding doctor: " + e.getMessage());
        }
    }

    /**
     * Adds an administrator to the database.
     *
     * @param admin The administrator to add.
     */
    public void addAdministrator(Administrator admin) {
        if (connection == null) return;
        try {
            String insertUserSql = "INSERT INTO users (user_id, name, contact_info, gender, role, email) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSql)) {
                userStmt.setString(1, admin.getUserID());
                userStmt.setString(2, admin.getName());
                userStmt.setString(3, admin.getContactInfo());
                userStmt.setString(4, admin.getGender());
                userStmt.setString(5, "ADMIN");
                userStmt.setString(6, admin.getEmail());
                userStmt.executeUpdate();
            }

            String insertAdminSql = "INSERT INTO administrators (user_id, password, joining_date) VALUES (?, ?, ?)";
            try (PreparedStatement adminStmt = connection.prepareStatement(insertAdminSql)) {
                adminStmt.setString(1, admin.getUserID());
                adminStmt.setString(2, admin.getPassword());
                adminStmt.setDate(3, java.sql.Date.valueOf(admin.getJoiningDate()));
                adminStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding administrator: " + e.getMessage());
        }
    }

    /**
     * Retrieves a user from the database by ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The user object, or null if not found.
     */
    public User getUser(String userId) {
        if (connection == null) return null;
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        switch (role) {
                            case "PATIENT": return getPatient(userId);
                            case "DOCTOR": return getDoctor(userId);
                            case "ADMIN": return getAdministrator(userId);
                            default: return null;
                        }
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a patient from the database by ID.
     *
     * @param userId The ID of the patient to retrieve.
     * @return The patient object, or null if not found.
     */
    private Patient getPatient(String userId) {
        if (connection == null) return null;
        try {
            String sql = "SELECT u.*, p.birth_date, p.admission_date, p.email, p.password " +
                    "FROM users u JOIN patients p ON u.user_id = p.user_id WHERE u.user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String contactInfo = rs.getString("contact_info");
                        String email = rs.getString("email");
                        String gender = rs.getString("gender");
                        String password = rs.getString("password");
                        LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
                        LocalDate admissionDate = rs.getDate("admission_date") != null ? rs.getDate("admission_date").toLocalDate() : null;
                        Patient patient = new Patient(userId, name, contactInfo, email, password, gender, birthDate);
                        patient.setAdmissionDate(admissionDate);
                        return patient;
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving patient: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a doctor from the database by ID.
     *
     * @param userId The ID of the doctor to retrieve.
     * @return The doctor object, or null if not found.
     */
    private Doctor getDoctor(String userId) {
        if (connection == null) return null;
        try {
            String sql = "SELECT u.*, d.joining_date, d.specialization, d.email, d.password " +
                    "FROM users u JOIN doctors d ON u.user_id = d.user_id WHERE u.user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String contactInfo = rs.getString("contact_info");
                        String email = rs.getString("email");
                        String gender = rs.getString("gender");
                        String password = rs.getString("password");
                        LocalDate joiningDate = rs.getDate("joining_date").toLocalDate();
                        String specialization = rs.getString("specialization");
                        return new Doctor(userId, name, contactInfo, email, password, gender, specialization, joiningDate);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving doctor: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves an administrator from the database by ID.
     *
     * @param userId The ID of the administrator to retrieve.
     * @return The administrator object, or null if not found.
     */
    private Administrator getAdministrator(String userId) {
        if (connection == null) return null;
        try {
            String sql = "SELECT u.*, a.password, a.joining_date " +
                    "FROM users u JOIN administrators a ON u.user_id = a.user_id WHERE u.user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String contactInfo = rs.getString("contact_info");
                        String email = rs.getString("email");
                        String gender = rs.getString("gender");
                        String password = rs.getString("password");
                        LocalDate joiningDate = rs.getDate("joining_date").toLocalDate();
                        return new Administrator(userId, name, contactInfo, email, password, gender, joiningDate);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving administrator: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds an appointment to the database.
     *
     * @param appointment The appointment to add.
     */
    public void addAppointment(Appointment appointment) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO appointments (appointment_time, patient_id, doctor_id, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, Timestamp.valueOf(appointment.getAppointmentTime()));
                stmt.setString(2, appointment.getPatient().getUserID());
                stmt.setString(3, appointment.getDoctor().getUserID());
                stmt.setString(4, appointment.getAppointmentStatus());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    appointment.setAppointmentID(rs.getLong(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding appointment: " + e.getMessage());
        }
    }

    /**
     * Updates the status of an appointment in the database.
     *
     * @param appointmentId The ID of the appointment to update.
     * @param status        The new status (e.g., Approved, Canceled).
     */
    public void updateAppointmentStatus(long appointmentId, String status) {
        if (connection == null) return;
        try {
            String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setLong(2, appointmentId);
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error updating appointment status: " + e.getMessage());
        }
    }

    /**
     * Retrieves pending appointments for a doctor from the database.
     *
     * @param doctorId The ID of the doctor.
     * @return A list of pending appointments for the doctor.
     */
    public List<Appointment> getPendingAppointmentsForDoctor(String doctorId) {
        List<Appointment> pending = new ArrayList<>();
        if (connection == null) return pending;
        try {
            String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND status = 'Pending'";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, doctorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Appointment app = new Appointment(
                                rs.getLong("appointment_id"),
                                rs.getTimestamp("appointment_time").toLocalDateTime(),
                                (Patient) getUser(rs.getString("patient_id")),
                                (Doctor) getUser(rs.getString("doctor_id"))
                        );
                        app.setAppointmentStatus(rs.getString("status"));
                        pending.add(app);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving pending appointments: " + e.getMessage());
        }
        return pending;
    }

    /**
     * Adds a vital sign record to the database.
     *
     * @param vital The vital sign to add.
     */
    public void addVitalSign(VitalSign vital) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO vitals (patient_id, checkup_date, heart_rate, blood_pressure, body_temperature, oxygen_level) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, vital.getPatientID());
                stmt.setDate(2, java.sql.Date.valueOf(vital.getCheckupDate()));
                stmt.setDouble(3, vital.getHeartRate());
                stmt.setDouble(4, vital.getBloodPressure());
                stmt.setDouble(5, vital.getBodyTemperature());
                stmt.setDouble(6, vital.getOxygenLevel());
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding vital sign: " + e.getMessage());
        }
    }

    /**
     * Retrieves vital signs for a patient from the database.
     *
     * @param patientId The ID of the patient.
     * @return A list of vital signs for the patient.
     */
    public List<VitalSign> getVitalSigns(String patientId) {
        List<VitalSign> vitals = new ArrayList<>();
        if (connection == null) return vitals;
        try {
            String sql = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY checkup_date DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, patientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        VitalSign vital = new VitalSign(
                                rs.getString("patient_id"),
                                rs.getDouble("heart_rate"),
                                rs.getDouble("blood_pressure"),
                                rs.getDouble("body_temperature"),
                                rs.getDouble("oxygen_level"),
                                rs.getDate("checkup_date").toLocalDate()
                        );
                        vitals.add(vital);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving vital signs: " + e.getMessage());
        }
        return vitals;
    }

    /**
     * Adds feedback to the database.
     *
     * @param feedback The feedback to add.
     */
    public void addFeedback(Feedback feedback) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO feedback (patient_id, doctor_id, date, comments) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, feedback.getPatientID());
                stmt.setString(2, feedback.getDoctorID());
                stmt.setDate(3, java.sql.Date.valueOf(feedback.getDate()));
                stmt.setString(4, feedback.getComments());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    feedback.setFeedbackID(rs.getLong(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding feedback: " + e.getMessage());
        }
    }

    /**
     * Retrieves feedback for a patient from the database.
     *
     * @param patientId The ID of the patient.
     * @return A list of feedback for the patient.
     */
    public List<Feedback> getFeedbacksForPatient(String patientId) {
        List<Feedback> feedbacks = new ArrayList<>();
        if (connection == null) return feedbacks;
        try {
            String sql = "SELECT * FROM feedback WHERE patient_id = ? ORDER BY date DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, patientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Feedback feedback = new Feedback(
                                rs.getLong("feedback_id"),
                                rs.getString("patient_id"),
                                rs.getString("doctor_id"),
                                rs.getDate("date").toLocalDate(),
                                rs.getString("comments")
                        );
                        feedbacks.add(feedback);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving feedbacks: " + e.getMessage());
        }
        return feedbacks;
    }

    /**
     * Adds a prescription to the database.
     *
     * @param prescription The prescription to add.
     */
    public void addPrescription(Prescription prescription) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO prescriptions (patient_id, medication, dosage, schedule) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, prescription.getPatientID());
                stmt.setString(2, prescription.getMedication());
                stmt.setString(3, prescription.getDosage());
                stmt.setString(4, prescription.getSchedule());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    prescription.setPrescriptionID(rs.getLong(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding prescription: " + e.getMessage());
        }
    }

    /**
     * Retrieves prescriptions for a patient from the database.
     *
     * @param patientId The ID of the patient.
     * @return A list of prescriptions for the patient.
     */
    public List<Prescription> getPrescriptionsForPatient(String patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        if (connection == null) return prescriptions;
        try {
            String sql = "SELECT * FROM prescriptions WHERE patient_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, patientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Prescription prescription = new Prescription(
                                rs.getLong("prescription_id"),
                                rs.getString("patient_id"),
                                rs.getString("medication"),
                                rs.getString("dosage"),
                                rs.getString("schedule")
                        );
                        prescriptions.add(prescription);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving prescriptions: " + e.getMessage());
        }
        return prescriptions;
    }

    /**
     * Adds a chat message to the database.
     *
     * @param senderId   The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param message    The message content.
     */
    public void addMessage(String senderId, String receiverId, String message) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, senderId);
                stmt.setString(2, receiverId);
                stmt.setString(3, message);
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding message: " + e.getMessage());
        }
    }

    /**
     * Retrieves messages between two users from the database.
     *
     * @param user1 The ID of the first user.
     * @param user2 The ID of the second user.
     * @return A list of messages between the two users.
     */
    public List<String> getMessagesBetween(String user1, String user2) {
        List<String> messages = new ArrayList<>();
        if (connection == null) return messages;
        try {
            String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user1);
                stmt.setString(2, user2);
                stmt.setString(3, user2);
                stmt.setString(4, user1);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String sender = rs.getString("sender_id");
                        String message = rs.getString("message");
                        messages.add(sender + ": " + message);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Adds a video consultation to the database.
     *
     * @param videoCall The video call to add.
     */
    public void addVideoConsultation(VideoCall videoCall) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO video_consultations (doctor_id, patient_id, scheduled_time, meeting_link) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, videoCall.getDoctorID());
                stmt.setString(2, videoCall.getPatientID());
                stmt.setTimestamp(3, Timestamp.valueOf(videoCall.getScheduledTime()));
                stmt.setString(4, videoCall.getMeetingLink());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    videoCall.setCallId(rs.getLong(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error adding video consultation: " + e.getMessage());
        }
    }

    /**
     * Removes a patient and their associated data from the database.
     *
     * @param patientId The ID of the patient to remove.
     */
    public void removePatient(String patientId) {
        if (connection == null) return;
        try {
            String[] deleteRelatedSqls = {
                    "DELETE FROM vitals WHERE patient_id = ?",
                    "DELETE FROM feedback WHERE patient_id = ?",
                    "DELETE FROM prescriptions WHERE patient_id = ?",
                    "DELETE FROM appointments WHERE patient_id = ?",
                    "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?",
                    "DELETE FROM video_consultations WHERE patient_id = ?"
            };
            for (String sql : deleteRelatedSqls) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, patientId);
                    if (sql.contains("messages")) {
                        stmt.setString(2, patientId);
                    }
                    stmt.executeUpdate();
                }
            }

            String deletePatientSql = "DELETE FROM patients WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deletePatientSql)) {
                stmt.setString(1, patientId);
                stmt.executeUpdate();
            }

            String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteUserSql)) {
                stmt.setString(1, patientId);
                stmt.executeUpdate();
            }

            logAction("Removed patient " + patientId + " on " + LocalDate.now());

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error removing patient: " + e.getMessage());
        }
    }

    public void removeDoctor(String doctorId) {
        if (connection == null) return;
        try {
            String[] deleteRelatedSqls = {
                    "DELETE FROM feedback WHERE doctor_id = ?",
                    "DELETE FROM appointments WHERE doctor_id = ?",
                    "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?",
                    "DELETE FROM video_consultations WHERE doctor_id = ?"
            };
            for (String sql : deleteRelatedSqls) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, doctorId);
                    if (sql.contains("messages")) {
                        stmt.setString(2, doctorId);
                    }
                    stmt.executeUpdate();
                }
            }

            String deleteDoctorSql = "DELETE FROM doctors WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteDoctorSql)) {
                stmt.setString(1, doctorId);
                stmt.executeUpdate();
            }

            String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteUserSql)) {
                stmt.setString(1, doctorId);
                stmt.executeUpdate();
            }

            logAction("Removed doctor " + doctorId + " on " + LocalDate.now());

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error removing doctor: " + e.getMessage());
        }
    }

    public void logAction(String action) {
        if (connection == null) return;
        try {
            String sql = "INSERT INTO system_logs (action, log_date) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, action);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.out.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error logging action: " + e.getMessage());
        }
    }

    public List<String> getSystemLogs() {
        List<String> logs = new ArrayList<>();
        if (connection == null) return logs;
        try {
            String sql = "SELECT * FROM system_logs ORDER BY log_date DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String log = rs.getString("action") + " (Date: " + rs.getDate("log_date") + ")";
                        logs.add(log);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving system logs: " + e.getMessage());
        }
        return logs;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

// Notifications and Reminders
class ReminderService {
    private final EmailNotification emailNotification;

    public ReminderService(EmailNotification emailNotification) {
        this.emailNotification = emailNotification != null ? emailNotification : new EmailNotification();
    }

    public void sendAppointmentReminder(Appointment appointment) {
        if (appointment == null) return;
        try {
            String message = "Reminder: Appointment with Dr. " + appointment.getDoctor().getName() +
                    " on " + appointment.getAppointmentTime();
            emailNotification.sendNotification(message, appointment.getPatient().getEmail());
        } catch (NotificationException e) {
            System.out.println("Error sending appointment reminder: " + e.getMessage());
        }
    }

    public void sendMedicationReminder(Prescription prescription) {
        if (prescription == null) return;
        try {
            DataBase db = DataBase.getInstance();
            User patient = db.getUser(prescription.getPatientID());
            String message = "Reminder: Take " + prescription.getMedication() +
                    " (" + prescription.getDosage() + ") as per schedule: " + prescription.getSchedule();
            emailNotification.sendNotification(message, patient.getEmail());
        } catch (NotificationException e) {
            System.out.println("Error sending medication reminder: " + e.getMessage());
        }
    }
}

// Health Trends Visualization Class
class HealthTrendsVisualizer {
    public LineChart<Number, Number> generateVitalTrendsChart(String patientId, DataBase db) {
        if (patientId == null || db == null) return null;

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Measurement Number");
        yAxis.setLabel("Value");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Vital Signs Trends for Patient " + patientId);

        List<VitalSign> vitals = db.getVitalSigns(patientId);
        if (vitals.isEmpty()) return lineChart;

        XYChart.Series<Number, Number> heartRateSeries = new XYChart.Series<>();
        heartRateSeries.setName("Heart Rate");
        XYChart.Series<Number, Number> bloodPressureSeries = new XYChart.Series<>();
        bloodPressureSeries.setName("Blood Pressure");
        XYChart.Series<Number, Number> temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Temperature");
        XYChart.Series<Number, Number> oxygenLevelSeries = new XYChart.Series<>();
        oxygenLevelSeries.setName("Oxygen Level");

        for (int i = 0; i < vitals.size(); i++) {
            VitalSign vital = vitals.get(i);
            heartRateSeries.getData().add(new XYChart.Data<>(i + 1, vital.getHeartRate()));
            bloodPressureSeries.getData().add(new XYChart.Data<>(i + 1, vital.getBloodPressure()));
            temperatureSeries.getData().add(new XYChart.Data<>(i + 1, vital.getBodyTemperature()));
            oxygenLevelSeries.getData().add(new XYChart.Data<>(i + 1, vital.getOxygenLevel()));
        }

        lineChart.getData().addAll(heartRateSeries, bloodPressureSeries, temperatureSeries, oxygenLevelSeries);
        return lineChart;
    }
}

// Report Generation Class
class ReportGenerator {
    public void generateAndSaveReport(String patientId, DataBase db, String filePath) {
        if (patientId == null || db == null || filePath == null) {
            System.out.println("Error: Invalid input parameters for report generation.");
            return;
        }

        // Ensure the parent directory exists
        File directory = new File(filePath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        // Use the provided filePath directly
        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println("Health Report for Patient " + patientId);
            writer.println("Generated on: " + LocalDate.now());
            writer.println("----------------------------------------");

            List<VitalSign> vitals = db.getVitalSigns(patientId);
            writer.println("\nVitals History:");
            writer.println("----------------------------------------");
            for (VitalSign vital : vitals) {
                writer.println(vital);
            }

            List<Feedback> feedbacks = db.getFeedbacksForPatient(patientId);
            writer.println("\nFeedback History:");
            writer.println("----------------------------------------");
            for (Feedback feedback : feedbacks) {
                writer.println(feedback);
            }

            List<Prescription> prescriptions = db.getPrescriptionsForPatient(patientId);
            writer.println("\nPrescription History:");
            writer.println("----------------------------------------");
            for (Prescription prescription : prescriptions) {
                writer.println(prescription);
            }

            writer.println("\nDoctor Recommendations:");
            writer.println("----------------------------------------");
            if (vitals.isEmpty()) {
                writer.println("No vital signs data available for recommendations.");
            } else {
                VitalSign latestVital = vitals.get(0);
                if (latestVital.getHeartRate() > 100) {
                    writer.println("- High heart rate detected. Recommend immediate rest and consultation.");
                }
                if (latestVital.getBloodPressure() > 140) {
                    writer.println("- High blood pressure detected. Recommend monitoring and possible medication adjustment.");
                }
                if (latestVital.getBodyTemperature() > 37.2) {
                    writer.println("- Elevated temperature detected. Recommend fever management and monitoring.");
                }
                if (latestVital.getOxygenLevel() < 95) {
                    writer.println("- Low oxygen level detected. Recommend oxygen therapy and immediate consultation.");
                }
                if (EmergencyAlert.isWithinThreshold(latestVital)) {
                    writer.println("- All vital signs within normal range. Continue regular monitoring.");
                }
            }

            System.out.println("Report generated and saved: " + filePath);
        } catch (IOException e) {
            System.out.println("Error generating report: " + e.getMessage());
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }
}


/* 
Main Class
of the application
*/
public class RPMSMain extends Application {
    private static DataBase db;
    private static ChatServer chatServer;
    private static EmailNotification emailNotification;
    private static SMSNotification smsNotification;
    private static ReminderService reminderService;
    private static FileHandling fileHandling;
    private static ReportGenerator reportGenerator;
    private static HealthTrendsVisualizer trendsVisualizer;
    private static Scanner scanner;

    private static void initializeSystem() {
        scanner = new Scanner(System.in);
        db = DataBase.getInstance();
        chatServer = new ChatServer(db);
        emailNotification = new EmailNotification();
        smsNotification = new SMSNotification();
        reminderService = new ReminderService(emailNotification);
        fileHandling = new FileHandling();
        reportGenerator = new ReportGenerator();
        trendsVisualizer = new HealthTrendsVisualizer();
        System.out.println("System initialized successfully.\n");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeSystem();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/oopproject/MainUI.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/com/example/oopproject/styles.css").toExternalForm());
        primaryStage.setTitle("Remote Patient Monitoring System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
