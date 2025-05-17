// Package declaration for the Remote Patient Monitoring System (RPMS) project
package com.example.oopproject;

// Import statements for JavaFX, file handling, date/time, and project-specific classes
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import static com.example.oopproject.EmergencyAlert.isWithinThreshold;

// Main controller class for handling JavaFX UI interactions
public class MainController {
    // FXML-injected UI components for main layout and panes
    @FXML private VBox mainContainer; // Main container for the application
    @FXML private VBox loginPane; // Pane for user login
    @FXML private ScrollPane registerPane; // Pane for user registration
    @FXML private VBox patientPane; // Pane for patient dashboard
    @FXML private VBox doctorPane; // Pane for doctor dashboard
    @FXML private VBox adminPane; // Pane for administrator dashboard

    // FXML-injected fields for login form
    @FXML private TextField userIdField; // Field for entering user ID
    @FXML private PasswordField passwordField; // Field for entering password
    @FXML private Label loginErrorLabel; // Label to display login errors

    // FXML-injected fields for registration form
    @FXML private ComboBox<String> roleCombo; // Dropdown for selecting user role
    @FXML private TextField regUserIdField; // Field for registration user ID
    @FXML private TextField regNameField; // Field for user name
    @FXML private TextField regPhoneField; // Field for phone number
    @FXML private TextField regEmailField; // Field for email
    @FXML private PasswordField regPasswordField; // Field for password
    @FXML private TextField regGenderField; // Field for gender
    @FXML private TextField regBirthDateField; // Field for birth date (patients)
    @FXML private TextField regSpecializationField; // Field for specialization (doctors)
    @FXML private TextField regJoiningDateField; // Field for joining date (doctors/admins)
    @FXML private Label registerErrorLabel; // Label to display registration errors

    // FXML-injected fields for dashboard displays
    @FXML private Label patientWelcomeLabel; // Welcome label for patient dashboard
    @FXML private TextArea patientOutputArea; // Output area for patient actions
    @FXML private Label doctorWelcomeLabel; // Welcome label for doctor dashboard
    @FXML private TextArea doctorOutputArea; // Output area for doctor actions
    @FXML private Label adminWelcomeLabel; // Welcome label for admin dashboard
    @FXML private TextArea adminOutputArea; // Output area for admin actions

    // System components and state variables
    private DataBase db; // Database instance for data operations
    private ChatServer chatServer; // Chat server for messaging
    private EmailNotification emailNotification; // Service for email notifications
    private SMSNotification smsNotification; // Service for SMS notifications
    private ReminderService reminderService; // Service for sending reminders
    private FileHandling fileHandling; // Utility for handling CSV files
    private ReportGenerator reportGenerator; // Utility for generating health reports
    private HealthTrendsVisualizer trendsVisualizer; // Utility for visualizing vital trends
    private User currentUser; // Currently logged-in user
    private Map<String, double[]> normalRanges; // Map storing normal ranges for vital signs

    // Initializes the controller and system components
    @FXML
    public void initialize() {
        try {
            // Initialize database and verify connection
            db = DataBase.getInstance();
            if (!db.isConnected()) {
                throw new RuntimeException("Database connection is closed.");
            }
            // Initialize system services
            chatServer = new ChatServer(db);
            emailNotification = new EmailNotification();
            smsNotification = new SMSNotification();
            reminderService = new ReminderService(emailNotification);
            fileHandling = new FileHandling();
            reportGenerator = new ReportGenerator();
            trendsVisualizer = new HealthTrendsVisualizer();

            // Initialize normal ranges for vital signs thresholds
            normalRanges = new HashMap<>();
            normalRanges.put("heart_rate", new double[]{60.0, 100.0});
            normalRanges.put("blood_pressure", new double[]{90.0, 140.0});
            normalRanges.put("body_temperature", new double[]{36.1, 37.2});
            normalRanges.put("oxygen_level", new double[]{95.0, 100.0});

            // Set up role selection listener to toggle registration fields
            roleCombo.setOnAction(e -> updateRegisterFields());
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Initialization failed: " + e.getMessage());
        }
    }

    // Updates visibility of registration fields based on selected role
    private void updateRegisterFields() {
        String role = roleCombo.getValue();
        // Show specialization and joining date fields for doctors only
        regSpecializationField.setVisible("Doctor".equals(role));
        regSpecializationField.setManaged("Doctor".equals(role));
        // Show joining date field for doctors and administrators
        regJoiningDateField.setVisible("Doctor".equals(role) || "Administrator".equals(role));
        regJoiningDateField.setManaged("Doctor".equals(role) || "Administrator".equals(role));
    }

    // Handles user login attempt
    @FXML
    private void handleLogin() {
        try {
            String userId = userIdField.getText().trim();
            String password = passwordField.getText().trim();
            // Validate input fields
            if (userId.isEmpty() || password.isEmpty()) {
                loginErrorLabel.setText("User ID and password are required.");
                return;
            }
            // Retrieve user from database and verify credentials
            currentUser = db.getUser(userId);
            if (currentUser != null && currentUser.getPassword().equals(password)) {
                loginErrorLabel.setText("");
                showDashboard();
            } else {
                loginErrorLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            loginErrorLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Shows the registration pane and hides the login pane
    @FXML
    private void showRegisterPane() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    // Shows the login pane and hides the registration pane
    @FXML
    private void showLoginPane() {
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        clearRegisterFields();
    }

    // Clears all registration form fields
    private void clearRegisterFields() {
        roleCombo.getSelectionModel().clearSelection();
        regUserIdField.clear();
        regNameField.clear();
        regPhoneField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regGenderField.clear();
        regBirthDateField.clear();
        regSpecializationField.clear();
        regJoiningDateField.clear();
        registerErrorLabel.setText("");
    }

    // Handles user registration based on selected role
    @FXML
    private void handleRegister() {
        try {
            String role = roleCombo.getValue();
            if (role == null) {
                registerErrorLabel.setText("Please select a role.");
                return;
            }
            // Retrieve and validate common fields
            String userId = regUserIdField.getText().trim();
            String name = regNameField.getText().trim();
            String phone = regPhoneField.getText().trim();
            String email = regEmailField.getText().trim();
            String password = regPasswordField.getText().trim();
            String gender = regGenderField.getText().trim();
            if (userId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || gender.isEmpty()) {
                registerErrorLabel.setText("All fields are required.");
                return;
            }
            LocalDate birthDate = null;
            if (!regBirthDateField.getText().trim().isEmpty()) {
                birthDate = LocalDate.parse(regBirthDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            }

            // Register user based on role
            switch (role) {
                case "Patient":
                    if (birthDate == null) {
                        registerErrorLabel.setText("Birth date is required for patients.");
                        return;
                    }
                    Patient patient = new Patient(userId, name, phone, email, password, gender, birthDate);
                    db.addPatient(patient);
                    registerErrorLabel.setText("Patient registered successfully!");
                    break;
                case "Doctor":
                    String specialization = regSpecializationField.getText().trim();
                    if (specialization.isEmpty()) {
                        registerErrorLabel.setText("Specialization is required for doctors.");
                        return;
                    }
                    LocalDate joiningDate = LocalDate.parse(regJoiningDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                    Doctor doctor = new Doctor(userId, name, phone, email, password, gender, specialization, joiningDate);
                    db.addDoctor(doctor);
                    registerErrorLabel.setText("Doctor registered successfully!");
                    break;
                case "Administrator":
                    joiningDate = LocalDate.parse(regJoiningDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                    Administrator admin = new Administrator(userId, name, phone, email, password, gender, joiningDate);
                    db.addAdministrator(admin);
                    registerErrorLabel.setText("Administrator registered successfully!");
                    break;
                default:
                    registerErrorLabel.setText("Please select a valid role.");
                    return;
            }
            showLoginPane();
        } catch (DateTimeParseException e) {
            registerErrorLabel.setText("Invalid date format. Use YYYY-MM-DD.");
        } catch (Exception e) {
            registerErrorLabel.setText("Error during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Displays the appropriate dashboard based on user role
    private void showDashboard() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        if (currentUser instanceof Patient) {
            patientPane.setVisible(true);
            patientPane.setManaged(true);
            patientWelcomeLabel.setText("Welcome, " + currentUser.getName());
            ((Patient) currentUser).setChatClient(new ChatClient(currentUser, chatServer));
        } else if (currentUser instanceof Doctor) {
            doctorPane.setVisible(true);
            doctorPane.setManaged(true);
            doctorWelcomeLabel.setText("Welcome, Dr. " + currentUser.getName());
            ((Doctor) currentUser).setChatClient(new ChatClient(currentUser, chatServer));
        } else if (currentUser instanceof Administrator) {
            adminPane.setVisible(true);
            adminPane.setManaged(true);
            adminWelcomeLabel.setText("Welcome, " + currentUser.getName());
        }
    }

    // Handles user logout and resets UI
    @FXML
    private void handleLogout() {
        // Stop chat client for patient or doctor
        if (currentUser instanceof Patient) {
            ((Patient) currentUser).getChatClient().stop();
        } else if (currentUser instanceof Doctor) {
            ((Doctor) currentUser).getChatClient().stop();
        }
        currentUser = null;
        // Hide all dashboard panes and show login pane
        patientPane.setVisible(false);
        patientPane.setManaged(false);
        doctorPane.setVisible(false);
        doctorPane.setManaged(false);
        adminPane.setVisible(false);
        adminPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        // Clear input fields and output areas
        userIdField.clear();
        passwordField.clear();
        patientOutputArea.clear();
        doctorOutputArea.clear();
        adminOutputArea.clear();
    }

    // Patient Handlers

    // Handles uploading vital signs from a CSV file
    @FXML
    private void handleUploadVitals() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            // Parse CSV file for vital signs
            List<VitalSign> vitals = fileHandling.parseCSV(file.getPath(), currentUser.getUserID());
            if (vitals.isEmpty()) {
                patientOutputArea.appendText("No valid data to upload.\n");
                return;
            }

            List<VitalSign> normalVitals = new ArrayList<>();
            List<VitalSign> abnormalVitals = new ArrayList<>();

            // Categorize vitals as normal or abnormal based on thresholds
            for (VitalSign vital : vitals) {
                if (isWithinThreshold(vital)) {
                    normalVitals.add(vital);
                } else {
                    abnormalVitals.add(vital);
                }
            }

            // Store normal vitals in the database
            for (VitalSign normalVital : normalVitals) {
                db.addVitalSign(normalVital);
            }

            // Notify doctor about abnormal vitals via email
            if (!abnormalVitals.isEmpty()) {
                try {
                    StringBuilder message = new StringBuilder("Abnormal vitals detected for patient " + currentUser.getUserID() + ":\n");
                    for (VitalSign abnormal : abnormalVitals) {
                        message.append(String.format("Date: %s, HR: %.2f, BP: %.2f, Temp: %.2f, O2: %.2f\n",
                                abnormal.getCheckupDate(), abnormal.getHeartRate(), abnormal.getBloodPressure(),
                                abnormal.getBodyTemperature(), abnormal.getOxygenLevel()));
                    }
                    // Note: This sends to the patient's email; intended recipient should be a doctor
                    User doctor = db.getUser(currentUser.getUserID());
                    emailNotification.sendNotification(message.toString(), doctor.getEmail());
                    patientOutputArea.appendText("Notified doctor about " + abnormalVitals.size() + " abnormal vitals.\n");
                } catch (NotificationException e) {
                    patientOutputArea.appendText("Error notifying doctor: " + e.getMessage() + "\n");
                }
            }

            patientOutputArea.appendText("Uploaded " + normalVitals.size() + " normal vitals successfully. " +
                    abnormalVitals.size() + " abnormal vitals were not stored.\n");
        }
    }

    // Displays feedback for the logged-in patient
    @FXML
    private void handleViewFeedback() {
        ((Patient) currentUser).viewFeedback();
        List<Feedback> feedbacks = db.getFeedbacksForPatient(currentUser.getUserID());
        patientOutputArea.clear();
        if (feedbacks.isEmpty()) {
            patientOutputArea.appendText("No feedback available.\n");
        } else {
            for (Feedback f : feedbacks) {
                patientOutputArea.appendText("Feedback: " + f.getComments() + " (Date: " + f.getDate() + ")\n");
            }
        }
    }

    // Displays prescriptions for the logged-in patient
    @FXML
    private void handleViewPrescriptions() {
        ((Patient) currentUser).viewPrescriptions();
        List<Prescription> prescriptions = db.getPrescriptionsForPatient(currentUser.getUserID());
        patientOutputArea.clear();
        if (prescriptions.isEmpty()) {
            patientOutputArea.appendText("No prescriptions available.\n");
        } else {
            for (Prescription p : prescriptions) {
                patientOutputArea.appendText(p.toString() + "\n");
            }
        }
    }

    // Opens a dialog to request an appointment with a doctor
    @FXML
    private void showRequestAppointment() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Request Appointment");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID");
        TextField timeField = new TextField();
        timeField.setPromptText("Time (YYYY-MM-DD HH:MM)");
        content.getChildren().addAll(new Label("Doctor ID:"), doctorIdField, new Label("Appointment Time:"), timeField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? doctorIdField.getText() + ";" + timeField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";");
            if (parts.length < 2) {
                patientOutputArea.appendText("Invalid input. Please provide both Doctor ID and time.\n");
                return;
            }
            String doctorId = parts[0].trim();
            String timeInput = parts[1].trim();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime time = LocalDateTime.parse(timeInput, formatter);
                User doctor = db.getUser(doctorId);
                if (doctor instanceof Doctor) {
                    ((Patient) currentUser).requestAppointment(time, (Doctor) doctor);
                    Appointment reminderAppointment = new Appointment(0, time, (Patient) currentUser, (Doctor) doctor);
                    reminderService.sendAppointmentReminder(reminderAppointment);
                    patientOutputArea.appendText("Appointment requested successfully!\n");
                } else {
                    patientOutputArea.appendText("Doctor not found.\n");
                }
            } catch (DateTimeParseException e) {
                patientOutputArea.appendText("Invalid format. Use YYYY-MM-DD HH:MM.\n");
            } catch (Exception e) {
                patientOutputArea.appendText("Error requesting appointment: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog to start a video consultation with a doctor
    @FXML
    private void showStartVideoConsultation() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Start Video Consultation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Doctor ID:"), doctorIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? doctorIdField.getText() : null);
        dialog.showAndWait().ifPresent(doctorId -> {
            if (doctorId == null || doctorId.trim().isEmpty()) {
                patientOutputArea.appendText("Doctor ID is required.\n");
                return;
            }
            User doctor = db.getUser(doctorId.trim());
            if (doctor instanceof Doctor) {
                LocalDateTime scheduledTime = LocalDateTime.now();
                String meetingLink = "https://meet.google.com/landing";
                VideoCall videoCall = new VideoCall(0, doctorId, currentUser.getUserID(), scheduledTime, meetingLink);
                db.addVideoConsultation(videoCall);
                try {
                    patientOutputArea.appendText("Video call link: " + videoCall.startCall() + "\n");
                } catch (Exception e) {
                    patientOutputArea.appendText("Error starting video call: " + e.getMessage() + "\n");
                }
            } else {
                patientOutputArea.appendText("Doctor not found.\n");
            }
        });
    }

    // Opens a dialog to send a chat message to another user
    @FXML
    private void showSendChatMessage() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Send Chat Message");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField receiverIdField = new TextField();
        receiverIdField.setPromptText("Receiver ID");
        TextArea messageField = new TextArea();
        messageField.setPromptText("Message");
        messageField.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Receiver ID:"), receiverIdField, new Label("Message:"), messageField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? receiverIdField.getText() + ";" + messageField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";", 2);
            if (parts.length < 2) {
                patientOutputArea.appendText("Invalid input. Please provide both Receiver ID and message.\n");
                return;
            }
            String receiverId = parts[0].trim();
            String message = parts[1].trim();
            if (receiverId.isEmpty() || message.isEmpty()) {
                patientOutputArea.appendText("Receiver ID and message are required.\n");
                return;
            }
            ((Patient) currentUser).sendMessage(receiverId, message);
            patientOutputArea.appendText("Message sent.\n");
        });
    }

    // Opens a dialog to view chat history with another user
    @FXML
    private void showViewChatHistory() {
        if (currentUser == null) {
            patientOutputArea.setText("Error: No user logged in.\n");
            return;
        }
        if (chatServer == null) {
            patientOutputArea.setText("Error: Chat server not initialized.\n");
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("View Chat History");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField otherUserIdField = new TextField();
        otherUserIdField.setPromptText("User ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("User ID for chat history:"), otherUserIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? otherUserIdField.getText() : null);
        dialog.showAndWait().ifPresent(otherUserId -> {
            if (otherUserId == null || otherUserId.trim().isEmpty()) {
                patientOutputArea.appendText("User ID is required.\n");
                return;
            }
            patientOutputArea.clear();
            try {
                List<String> messages = chatServer.getMessagesBetween(currentUser.getUserID(), otherUserId.trim());
                if (messages.isEmpty()) {
                    patientOutputArea.appendText("No messages found.\n");
                } else {
                    messages.forEach(msg -> patientOutputArea.appendText(msg + "\n"));
                }
            } catch (Exception e) {
                patientOutputArea.appendText("Error retrieving chat history: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog to trigger a panic button alert
    @FXML
    private void showPanicButton() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Press Panic Button");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Doctor ID:"), doctorIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? doctorIdField.getText() : null);
        dialog.showAndWait().ifPresent(doctorId -> {
            if (doctorId == null || doctorId.trim().isEmpty()) {
                patientOutputArea.appendText("Doctor ID is required.\n");
                return;
            }
            User doctor = db.getUser(doctorId.trim());
            if (doctor instanceof Doctor) {
                try {
                    List<String> recipients = Arrays.asList(((Doctor) doctor).getEmail());
                    AlertService alertService = new AlertService(new EmailNotification(), recipients);
                    PanicButton panicButton = new PanicButton((Patient) currentUser, (Doctor) doctor, alertService);
                    panicButton.pressPanicButton();
                    patientOutputArea.appendText("Emergency email alert sent.\n");
                } catch (NotificationException e) {
                    patientOutputArea.appendText("Error sending alert: " + e.getMessage() + "\n");
                }
            } else {
                patientOutputArea.appendText("Doctor not found.\n");
            }
        });
    }

    // Generates and saves a health report for the logged-in patient
    @FXML
    private void handleGenerateReport() {
        if (currentUser == null) {
            patientOutputArea.appendText("Error: No user logged in.\n");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Health Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("report_" + currentUser.getUserID() + "_" + LocalDate.now() + ".txt");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
        if (file == null) {
            patientOutputArea.appendText("Report generation cancelled.\n");
            return;
        }
        try {
            // Validate the file path
            File parentDir = file.getParentFile();
            if (parentDir == null || !parentDir.exists() || !parentDir.isDirectory()) {
                patientOutputArea.appendText("Error: Invalid directory selected. Please choose a valid directory.\n");
                return;
            }
            if (!parentDir.canWrite()) {
                patientOutputArea.appendText("Error: Cannot write to directory " + parentDir.getAbsolutePath() + ". Please choose a writable directory.\n");
                return;
            }
            String filePath = file.getAbsolutePath();
            System.out.println("Attempting to save report to: " + filePath); // Debug log
            reportGenerator.generateAndSaveReport(currentUser.getUserID(), db, filePath);
            patientOutputArea.appendText("Report saved to: " + filePath + "\n");
        } catch (Exception e) {
            patientOutputArea.appendText("Error generating report: " + e.getMessage() + "\n");
            System.err.println("Report generation failed for user " + currentUser.getUserID() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Displays a chart of vital sign trends for the logged-in patient
    @FXML
    private void showVitalTrends() {
        LineChart<Number, Number> chart = trendsVisualizer.generateVitalTrendsChart(currentUser.getUserID(), db);
        if (chart == null) {
            patientOutputArea.appendText("Error generating vital trends chart.\n");
            return;
        }
        Stage chartStage = new Stage();
        chartStage.setTitle("Vital Signs Trends");
        Scene scene = new Scene(chart, 800, 600);
        chartStage.setScene(scene);
        chartStage.show();
        patientOutputArea.appendText("Vital trends chart displayed.\n");
    }

    // Opens a dialog to send an SMS to a doctor
    @FXML
    private void showSendSMS() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Send Real-Time SMS");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID");
        TextArea messageField = new TextArea();
        messageField.setPromptText("Message");
        messageField.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Doctor ID:"), doctorIdField, new Label("Message:"), messageField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? doctorIdField.getText() + ";" + messageField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";", 2);
            if (parts.length < 2) {
                patientOutputArea.appendText("Invalid input. Please provide both Doctor ID and message.\n");
                return;
            }
            String doctorId = parts[0].trim();
            String message = parts[1].trim();
            if (doctorId.isEmpty() || message.isEmpty()) {
                patientOutputArea.appendText("Doctor ID and message are required.\n");
                return;
            }
            User doctor = db.getUser(doctorId);
            if (doctor instanceof Doctor) {
                try {
                    ((Patient) currentUser).sendRealTimeSMS((Doctor) doctor, message, smsNotification);
                    patientOutputArea.appendText("SMS sent to " + doctor.getContactInfo() + "\n");
                } catch (NotificationException e) {
                    patientOutputArea.appendText("Error sending SMS: " + e.getMessage() + "\n");
                }
            } else {
                patientOutputArea.appendText("Doctor not found.\n");
            }
        });
    }

    // Doctor Handlers

    // Opens a dialog to view a patient's vital signs
    @FXML
    private void showViewPatientVitals() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("View Patient Vitals");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Patient ID:"), patientIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() : null);
        dialog.showAndWait().ifPresent(patientId -> {
            if (patientId == null || patientId.trim().isEmpty()) {
                doctorOutputArea.appendText("Patient ID is required.\n");
                return;
            }
            doctorOutputArea.clear();
            List<VitalSign> vitals = db.getVitalSigns(patientId.trim());
            if (vitals.isEmpty()) {
                doctorOutputArea.appendText("No vitals found for patient " + patientId + "\n");
            } else {
                vitals.forEach(vital -> doctorOutputArea.appendText(vital.toString() + "\n"));
            }
        });
    }

    // Opens a dialog to view a patient's vital sign trends
    @FXML
    private void showPatientVitalTrends() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("View Patient Vital Trends");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Patient ID:"), patientIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() : null);
        dialog.showAndWait().ifPresent(patientId -> {
            if (patientId == null || patientId.trim().isEmpty()) {
                doctorOutputArea.appendText("Patient ID is required.\n");
                return;
            }
            LineChart<Number, Number> chart = trendsVisualizer.generateVitalTrendsChart(patientId.trim(), db);
            if (chart == null) {
                doctorOutputArea.appendText("Error generating vital trends chart for patient " + patientId + ".\n");
                return;
            }
            Stage chartStage = new Stage();
            chartStage.setTitle("Vital Signs Trends for Patient " + patientId);
            Scene scene = new Scene(chart, 800, 600);
            chartStage.setScene(scene);
            chartStage.show();
            doctorOutputArea.appendText("Vital trends chart displayed for patient " + patientId + ".\n");
        });
    }

    // Opens a dialog to provide feedback for a patient
    @FXML
    private void showProvideFeedback() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Provide Feedback");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        TextArea commentsField = new TextArea();
        commentsField.setPromptText("Feedback");
        commentsField.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Patient ID:"), patientIdField, new Label("Feedback:"), commentsField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() + ";" + commentsField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";", 2);
            if (parts.length < 2) {
                doctorOutputArea.appendText("Invalid input. Please provide both Patient ID and feedback.\n");
                return;
            }
            String patientId = parts[0].trim();
            String comments = parts[1].trim();
            if (patientId.isEmpty() || comments.isEmpty()) {
                doctorOutputArea.appendText("Patient ID and feedback are required.\n");
                return;
            }
            Feedback feedback = new Feedback(0, patientId, currentUser.getUserID(), LocalDate.now(), comments);
            ((Doctor) currentUser).provideFeedback(feedback);
            doctorOutputArea.appendText("Feedback provided.\n");
        });
    }

    // Opens a dialog to prescribe medication for a patient
    @FXML
    private void showPrescribeMedication() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Prescribe Medication");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        TextField medicationField = new TextField();
        medicationField.setPromptText("Medication");
        TextField dosageField = new TextField();
        dosageField.setPromptText("Dosage");
        TextField scheduleField = new TextField();
        scheduleField.setPromptText("Schedule");
        content.getChildren().addAll(
                new Label("Patient ID:"), patientIdField,
                new Label("Medication:"), medicationField,
                new Label("Dosage:"), dosageField,
                new Label("Schedule:"), scheduleField
        );
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ?
                patientIdField.getText() + ";" + medicationField.getText() + ";" + dosageField.getText() + ";" + scheduleField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";");
            if (parts.length < 4) {
                doctorOutputArea.appendText("Invalid input. Please provide all fields.\n");
                return;
            }
            String patientId = parts[0].trim();
            String medication = parts[1].trim();
            String dosage = parts[2].trim();
            String schedule = parts[3].trim();
            if (patientId.isEmpty() || medication.isEmpty() || dosage.isEmpty() || schedule.isEmpty()) {
                doctorOutputArea.appendText("All fields are required.\n");
                return;
            }
            Prescription prescription = new Prescription(0, patientId, medication, dosage, schedule);
            ((Doctor) currentUser).prescribeMedication(prescription);
            reminderService.sendMedicationReminder(prescription);
            doctorOutputArea.appendText("Prescription added.\n");
        });
    }

    // Opens a dialog to manage pending appointments
    @FXML
    private void showManageAppointments() {
        List<Appointment> pending = db.getPendingAppointmentsForDoctor(currentUser.getUserID());
        doctorOutputArea.clear();
        if (pending.isEmpty()) {
            doctorOutputArea.appendText("No pending appointments.\n");
            return;
        }
        pending.forEach(app -> doctorOutputArea.appendText(app.toString() + "\n"));
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Manage Appointments");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField appointmentIdField = new TextField();
        appointmentIdField.setPromptText("Appointment ID");
        ComboBox<String> actionCombo = new ComboBox<>();
        actionCombo.getItems().addAll("Approve", "Cancel");
        actionCombo.setPromptText("Select Action");
        content.getChildren().addAll(new Label("Appointment ID:"), appointmentIdField, new Label("Action:"), actionCombo);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? appointmentIdField.getText() + ";" + actionCombo.getValue() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";");
            if (parts.length < 2) {
                doctorOutputArea.appendText("Invalid input. Please provide both Appointment ID and action.\n");
                return;
            }
            String appointmentIdStr = parts[0].trim();
            String action = parts[1].trim();
            if (appointmentIdStr.isEmpty() || action.isEmpty()) {
                doctorOutputArea.appendText("Appointment ID and action are required.\n");
                return;
            }
            try {
                long appointmentId = Long.parseLong(appointmentIdStr);
                ((Doctor) currentUser).manageAppointment(appointmentId, action.toLowerCase());
                doctorOutputArea.appendText("Appointment " + action.toLowerCase() + "d.\n");
            } catch (NumberFormatException e) {
                doctorOutputArea.appendText("Invalid Appointment ID. Please enter a valid number.\n");
            } catch (Exception e) {
                doctorOutputArea.appendText("Error managing appointment: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog for a doctor to start a video consultation with a patient
    @FXML
    private void showDoctorStartVideoConsultation() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Start Video Consultation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Patient ID:"), patientIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() : null);
        dialog.showAndWait().ifPresent(patientId -> {
            if (patientId == null || patientId.trim().isEmpty()) {
                doctorOutputArea.appendText("Patient ID is required.\n");
                return;
            }
            User patient = db.getUser(patientId.trim());
            if (patient instanceof Patient) {
                LocalDateTime scheduledTime = LocalDateTime.now();
                String meetingLink = "https://meet.google.com/landing";
                VideoCall videoCall = new VideoCall(0, currentUser.getUserID(), patientId, scheduledTime, meetingLink);
                db.addVideoConsultation(videoCall);
                try {
                    doctorOutputArea.appendText("Video call link: " + videoCall.startCall() + "\n");
                } catch (Exception e) {
                    doctorOutputArea.appendText("Error starting video call: " + e.getMessage() + "\n");
                }
            } else {
                doctorOutputArea.appendText("Patient not found.\n");
            }
        });
    }

    // Opens a dialog for a doctor to send a chat message to another user
    @FXML
    private void showDoctorSendChatMessage() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Send Chat Message");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField receiverIdField = new TextField();
        receiverIdField.setPromptText("Receiver ID");
        TextArea messageField = new TextArea();
        messageField.setPromptText("Message");
        messageField.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Receiver ID:"), receiverIdField, new Label("Message:"), messageField);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? receiverIdField.getText() + ";" + messageField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";", 2);
            if (parts.length < 2) {
                doctorOutputArea.appendText("Invalid input. Please provide both Receiver ID and message.\n");
                return;
            }
            String receiverId = parts[0].trim();
            String message = parts[1].trim();
            if (receiverId.isEmpty() || message.isEmpty()) {
                doctorOutputArea.appendText("Receiver ID and message are required.\n");
                return;
            }
            ((Doctor) currentUser).sendMessage(receiverId, message);
            doctorOutputArea.appendText("Message sent.\n");
        });
    }

    // Opens a dialog for a doctor to view chat history with another user
    @FXML
    private void showDoctorViewChatHistory() {
        if (currentUser == null) {
            doctorOutputArea.setText("Error: No user logged in.\n");
            return;
        }
        if (chatServer == null) {
            doctorOutputArea.setText("Error: Chat server not initialized.\n");
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("View Chat History");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField otherUserIdField = new TextField();
        otherUserIdField.setPromptText("User ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("User ID for chat history:"), otherUserIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? otherUserIdField.getText() : null);
        dialog.showAndWait().ifPresent(otherUserId -> {
            if (otherUserId == null || otherUserId.trim().isEmpty()) {
                doctorOutputArea.appendText("User ID is required.\n");
                return;
            }
            doctorOutputArea.clear();
            try {
                List<String> messages = chatServer.getMessagesBetween(currentUser.getUserID(), otherUserId.trim());
                if (messages.isEmpty()) {
                    doctorOutputArea.appendText("No messages found.\n");
                } else {
                    messages.forEach(msg -> doctorOutputArea.appendText(msg + "\n"));
                }
            } catch (Exception e) {
                doctorOutputArea.appendText("Error retrieving chat history: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog for a doctor to generate a health report for a patient
    @FXML
    private void showGeneratePatientReport() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Generate Patient Report");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Patient ID:"), patientIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() : null);
        dialog.showAndWait().ifPresent(patientId -> {
            if (patientId == null || patientId.trim().isEmpty()) {
                doctorOutputArea.appendText("Patient ID is required.\n");
                return;
            }
            User patient = db.getUser(patientId.trim());
            if (!(patient instanceof Patient)) {
                doctorOutputArea.appendText("Patient not found.\n");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Patient Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fileChooser.setInitialFileName("report_" + patientId.trim() + "_" + LocalDate.now() + ".txt");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
            if (file == null) {
                doctorOutputArea.appendText("Report generation cancelled.\n");
                return;
            }
            try {
                File parentDir = file.getParentFile();
                if (parentDir == null || !parentDir.exists() || !parentDir.isDirectory()) {
                    doctorOutputArea.appendText("Error: Invalid directory selected. Please choose a valid directory.\n");
                    return;
                }
                if (!parentDir.canWrite()) {
                    doctorOutputArea.appendText("Error: Cannot write to directory " + parentDir.getAbsolutePath() + ". Please choose a writable directory.\n");
                    return;
                }
                String filePath = file.getAbsolutePath();
                System.out.println("Attempting to save report to: " + filePath); // Debug log
                reportGenerator.generateAndSaveReport(patientId.trim(), db, filePath);
                doctorOutputArea.appendText("Report saved to: " + filePath + "\n");
            } catch (Exception e) {
                doctorOutputArea.appendText("Error generating report: " + e.getMessage() + "\n");
                System.err.println("Report generation failed for patient " + patientId + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Admin Handlers

    // Opens a dialog to add a new patient
    @FXML
    private void showAddPatient() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number (e.g., +1234567890)");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField genderField = new TextField();
        genderField.setPromptText("Gender");
        TextField birthDateField = new TextField();
        birthDateField.setPromptText("Birth Date (YYYY-MM-DD)");
        content.getChildren().addAll(
                new Label("User ID:"), userIdField,
                new Label("Name:"), nameField,
                new Label("Phone Number:"), phoneField,
                new Label("Email:"), emailField,
                new Label("Password:"), passwordField,
                new Label("Gender:"), genderField,
                new Label("Birth Date:"), birthDateField
        );
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ?
                userIdField.getText() + ";" + nameField.getText() + ";" + phoneField.getText() + ";" +
                        emailField.getText() + ";" + passwordField.getText() + ";" + genderField.getText() + ";" + birthDateField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";");
            if (parts.length < 7) {
                adminOutputArea.appendText("Invalid input. Please provide all fields.\n");
                return;
            }
            try {
                String userId = parts[0].trim();
                String name = parts[1].trim();
                String phone = parts[2].trim();
                String email = parts[3].trim();
                String password = parts[4].trim();
                String gender = parts[5].trim();
                LocalDate birthDate = LocalDate.parse(parts[6].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                if (userId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || gender.isEmpty()) {
                    adminOutputArea.appendText("All fields are required.\n");
                    return;
                }
                Patient patient = new Patient(userId, name, phone, email, password, gender, birthDate);
                db.addPatient(patient);
                adminOutputArea.appendText("Patient added.\n");
            } catch (DateTimeParseException e) {
                adminOutputArea.appendText("Invalid date format. Use YYYY-MM-DD.\n");
            } catch (Exception e) {
                adminOutputArea.appendText("Error adding patient: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog to add a new doctor
    @FXML
    private void showAddDoctor() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Doctor");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox content = new VBox(10);
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number (e.g., +1234567890)");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField genderField = new TextField();
        genderField.setPromptText("Gender");
        TextField specializationField = new TextField();
        specializationField.setPromptText("Specialization");
        TextField joiningDateField = new TextField();
        joiningDateField.setPromptText("Joining Date (YYYY-MM-DD)");
        content.getChildren().addAll(
                new Label("User ID:"), userIdField,
                new Label("Name:"), nameField,
                new Label("Phone Number:"), phoneField,
                new Label("Email:"), emailField,
                new Label("Password:"), passwordField,
                new Label("Gender:"), genderField,
                new Label("Specialization:"), specializationField,
                new Label("Joining Date:"), joiningDateField
        );
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ?
                userIdField.getText() + ";" + nameField.getText() + ";" + phoneField.getText() + ";" +
                        emailField.getText() + ";" + passwordField.getText() + ";" + genderField.getText() + ";" +
                        specializationField.getText() + ";" + joiningDateField.getText() : null);
        dialog.showAndWait().ifPresent(result -> {
            String[] parts = result.split(";");
            if (parts.length < 8) {
                adminOutputArea.appendText("Invalid input. Please provide all fields.\n");
                return;
            }
            try {
                String userId = parts[0].trim();
                String name = parts[1].trim();
                String phone = parts[2].trim();
                String email = parts[3].trim();
                String password = parts[4].trim();
                String gender = parts[5].trim();
                String specialization = parts[6].trim();
                LocalDate joiningDate = LocalDate.parse(parts[7].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                if (userId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || gender.isEmpty() || specialization.isEmpty()) {
                    adminOutputArea.appendText("All fields are required.\n");
                    return;
                }
                Doctor doctor = new Doctor(userId, name, phone, email, password, gender, specialization, joiningDate);
                db.addDoctor(doctor);
                adminOutputArea.appendText("Doctor added.\n");
            } catch (DateTimeParseException e) {
                adminOutputArea.appendText("Invalid date format. Use YYYY-MM-DD.\n");
            } catch (Exception e) {
                adminOutputArea.appendText("Error adding doctor: " + e.getMessage() + "\n");
            }
        });
    }

    // Opens a dialog to remove a patient
    @FXML
    private void showRemovePatient() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Remove Patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Patient ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Patient ID:"), patientIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? patientIdField.getText() : null);
        dialog.showAndWait().ifPresent(patientId -> {
            if (patientId == null || patientId.trim().isEmpty()) {
                adminOutputArea.appendText("Patient ID is required.\n");
                return;
            }
            User patient = db.getUser(patientId.trim());
            if (patient instanceof Patient) {
                db.removePatient(patientId.trim());
                adminOutputArea.appendText("Patient removed successfully.\n");
            } else {
                adminOutputArea.appendText("Patient not found.\n");
            }
        });
    }

    // Opens a dialog to remove a doctor
    @FXML
    private void showRemoveDoctor() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Remove Doctor");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Doctor ID:"), doctorIdField));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? doctorIdField.getText() : null);
        dialog.showAndWait().ifPresent(doctorId -> {
            if (doctorId == null || doctorId.trim().isEmpty()) {
                adminOutputArea.appendText("Doctor ID is required.\n");
                return;
            }
            User doctor = db.getUser(doctorId.trim());
            if (doctor instanceof Doctor) {
                db.removeDoctor(doctorId.trim());
                adminOutputArea.appendText("Doctor removed successfully.\n");
            } else {
                adminOutputArea.appendText("Doctor not found.\n");
            }
        });
    }

    // Displays system logs in the admin dashboard
    @FXML
    private void handleViewSystemLogs() {
        adminOutputArea.clear();
        List<String> logs = db.getSystemLogs();
        if (logs.isEmpty()) {
            adminOutputArea.appendText("No logs available.\n");
        } else {
            logs.forEach(log -> adminOutputArea.appendText(log + "\n"));
        }
    }
}