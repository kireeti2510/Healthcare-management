package com.healthcare;

import com.healthcare.controller.*;
import com.healthcare.db.DatabaseConnection;
import com.healthcare.db.SchemaInitializer;
import com.healthcare.model.*;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.model.enums.Role;
import com.healthcare.pattern.creational.UserFactory;
import com.healthcare.pattern.structural.ClinicFacade;
import com.healthcare.repository.*;
import com.healthcare.service.*;
import com.healthcare.util.PasswordUtil;
import com.healthcare.view.ConsoleView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Entry point — wires all layers together (manual DI, no framework).
 * Healthcare Appointment and Patient Record Manager
 * Team E12
 */
public class Main {
    public static void main(String[] args) {
        // 1. DB init (Singleton pattern kicks in here)
        SchemaInitializer.initialize();

        // 2. Build dependency graph manually
        IApptRepository      apptRepo   = new ApptRepositoryImpl();
        IWaitlistRepository  waitlistRepo = new WaitlistRepositoryImpl();
        IPatientRepository   patRepo    = new PatientRepositoryImpl();
        IPrescriptionRepository rxRepo  = new PrescriptionRepositoryImpl();
        IMedicalRecordRepository mrRepo = new MedicalRecordRepositoryImpl();
        IAuditLogService     auditLog   = new AuditLogServiceImpl();

        INotificationService notifSvc   = new EmailSMSNotifService("SMTP-Client", "SMS-Gateway");

        IPatientService      patSvc     = new PatientServiceImpl(patRepo, auditLog);
        IAppointmentService  apptSvc    = new AppointmentServiceImpl(apptRepo, auditLog, notifSvc, waitlistRepo);
        IPrescriptionService rxSvc      = new PrescriptionServiceImpl(rxRepo, apptRepo, auditLog, notifSvc);
        IMedicalRecordService mrSvc     = new MedicalRecordServiceImpl(mrRepo, auditLog);
        ClinicAdminServiceImpl adminSvc = new ClinicAdminServiceImpl(auditLog, notifSvc);

        // 3. MVC Controllers
        PatientController      patCtrl  = new PatientController(patSvc);
        AppointmentController  apptCtrl = new AppointmentController(apptSvc);
        PrescriptionController rxCtrl   = new PrescriptionController(rxSvc);
        MedicalRecordController mrCtrl  = new MedicalRecordController(mrSvc);
        AdminController        adminCtrl= new AdminController(adminSvc);
        AuthController         authCtrl = new AuthController(auditLog);

        // 4. View
        ConsoleView view = new ConsoleView();

        // ---- Demo flow ----
        view.showMessage("=== Healthcare System Boot ===");
        view.showMessage("=== Teacher Demo: Java Feature Evidence ===");

        // Register patient (Builder pattern under the hood)
        PatientDTO dto = new PatientDTO("jane.doe@email.com", "pass123",
                LocalDate.of(1990, 5, 12), "INS-001", List.of("penicillin"));
        Patient patient = patCtrl.register(dto);
        view.showPatient(patient);

        // Factory pattern: create a clinician user object
        User clinician = UserFactory.createUser(Role.CLINICIAN, UUID.randomUUID(),
                "dr.smith@clinic.com", PasswordUtil.hash("docpass"), "LIC-999", "Cardiology");
        persistUser(clinician);
        view.showMessage("Clinician created: " + clinician.getEmail());

        User receptionist = UserFactory.createUser(Role.RECEPTIONIST, UUID.randomUUID(),
                "frontdesk@clinic.com", PasswordUtil.hash("deskpass"), "REC-101", "Morning");
        persistUser(receptionist);

        User pharmacist = UserFactory.createUser(Role.PHARMACIST, UUID.randomUUID(),
                "pharm@clinic.com", PasswordUtil.hash("pharmpass"), "REG-202", "Main Wing");
        persistUser(pharmacist);

        User admin = UserFactory.createUser(Role.CLINIC_ADMIN, UUID.randomUUID(),
                "admin@clinic.com", PasswordUtil.hash("adminpass"), "L2", null);
        persistUser(admin);

        showPermissionMatrix(view, patient, clinician, receptionist, pharmacist, admin);

        // Auth lock demo: after 5 wrong password attempts, login is temporarily blocked.
        for (int i = 1; i <= 6; i++) {
            boolean ok = authCtrl.login(clinician, "wrong-pass");
            view.showMessage("Login attempt " + i + " success=" + ok + " -> " + authCtrl.getLastAuthMessage());
        }

        boolean loginAfterWrongAttempts = authCtrl.login(clinician, "docpass");
        view.showMessage("Valid password after lock success=" + loginAfterWrongAttempts
                + " -> " + authCtrl.getLastAuthMessage());

        // Schedule appointment (Command + Notification triggered inside)
        Appointment appt = apptCtrl.scheduleAppointment(
                patient.getUserId(), clinician.getUserId(), "2025-09-15T10:30:00");
        view.showAppointment(appt);

        Appointment apptToCancel = apptCtrl.scheduleAppointment(
                patient.getUserId(), clinician.getUserId(), "2025-09-16T12:00:00");
        apptCtrl.cancelAppointmentByPatient(patient.getUserId(), apptToCancel.getAppointmentId());
        Appointment cancelledByPatient = apptRepo.findById(apptToCancel.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Cancelled appointment not found"));
        view.showMessage("Patient-cancelled appointment status: " + cancelledByPatient.getStatus());

        // Medical record workflow
        MedicalRecord record = mrCtrl.createRecord(patient.getUserId(), clinician);
        mrCtrl.addEncounterNote(record.getRecordId(), "Initial consult and vitals captured.", clinician);
        view.showMessage("Medical record state after note: "
                + mrCtrl.getRecord(record.getRecordId(), clinician).getState());

        // Create and check prescription (Chain of Responsibility)
        Prescription rx = rxCtrl.createPrescription(appt);
        Drug amoxicillin = new Drug("DRUG-AMOX-500", "Amoxicillin 500mg", List.of("penicillin"));
        rxCtrl.addPrescriptionItem(rx.getRxId(), new PrescriptionItem(amoxicillin, "500mg", "BID", 5));
        view.showMessage("Prescription status before issue: " + rx.getStatus());
        ConflictResult cr = rxCtrl.checkConflicts(rx.getRxId());
        view.showConflict(cr);
        ConflictResult review = rxCtrl.reviewPrescription(rx.getRxId(), "Clinician verified and approved after review");
        view.showMessage("Review completed. Conflict found: " + review.isHasConflict());
        rxCtrl.issuePrescription(rx.getRxId(), "pharmacist@clinic.com", "Clinician approved with documented review");
        Prescription updatedRx = rxRepo.findById(rx.getRxId())
                .orElseThrow(() -> new RuntimeException("Prescription not found after issue: " + rx.getRxId()));
        view.showMessage("Prescription status after issue: " + updatedRx.getStatus());
        rxCtrl.dispensePrescription(rx.getRxId());
        Prescription dispensedRx = rxRepo.findById(rx.getRxId())
                .orElseThrow(() -> new RuntimeException("Prescription not found after dispense: " + rx.getRxId()));
        view.showMessage("Prescription status after dispense: " + dispensedRx.getStatus());

        Prescription rxToVoid = rxCtrl.createPrescription(apptToCancel);
        rxCtrl.voidPrescription(rxToVoid.getRxId(), "Patient declined medication");
        Prescription voidedRx = rxRepo.findById(rxToVoid.getRxId())
                .orElseThrow(() -> new RuntimeException("Prescription not found after void: " + rxToVoid.getRxId()));
        view.showMessage("Prescription status after void: " + voidedRx.getStatus());
        Appointment completedFromIssue = apptRepo.findById(appt.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found after RX issue: " + appt.getAppointmentId()));
        view.showMessage("Appointment status after RX issue: " + completedFromIssue.getStatus());

        // Facade: combined workflow
        ClinicFacade facade = new ClinicFacade(patSvc, apptSvc, rxSvc);
        view.showMessage("Facade ready.");

        // Admin audit logs
        List<String> logs = adminCtrl.getAuditLogs(patient.getUserId());
        view.showAuditLogs(logs);

        // Shutdown
        DatabaseConnection.getInstance().close();
        view.showMessage("=== System shutdown ===");
    }

        private static void showPermissionMatrix(
                        ConsoleView view,
                        User patient,
                        User clinician,
                        User receptionist,
                        User pharmacist,
                        User admin
        ) {
                view.showMessage("--- Role Permission Matrix (Java hasPermission checks) ---");
                view.showMessage("PATIENT -> VIEW_OWN_APPOINTMENTS = " + patient.hasPermission("VIEW_OWN_APPOINTMENTS"));
                view.showMessage("PATIENT -> MANAGE_USERS = " + patient.hasPermission("MANAGE_USERS"));

                view.showMessage("CLINICIAN -> CREATE_PRESCRIPTION = " + clinician.hasPermission("CREATE_PRESCRIPTION"));
                view.showMessage("CLINICIAN -> DISPENSE_MEDICATION = " + clinician.hasPermission("DISPENSE_MEDICATION"));

                view.showMessage("RECEPTIONIST -> SCHEDULE_APPOINTMENT = " + receptionist.hasPermission("SCHEDULE_APPOINTMENT"));
                view.showMessage("RECEPTIONIST -> ISSUE_PRESCRIPTION = " + receptionist.hasPermission("ISSUE_PRESCRIPTION"));

                view.showMessage("PHARMACIST -> DISPENSE_MEDICATION = " + pharmacist.hasPermission("DISPENSE_MEDICATION"));
                view.showMessage("PHARMACIST -> MANAGE_USERS = " + pharmacist.hasPermission("MANAGE_USERS"));

                view.showMessage("ADMIN -> MANAGE_USERS = " + admin.hasPermission("MANAGE_USERS"));
                view.showMessage("ADMIN -> REVIEW_AUDIT_LOGS = " + admin.hasPermission("REVIEW_AUDIT_LOGS"));
        }

        private static void persistUser(User user) {
                String sql = "MERGE INTO users(user_id,email,password_hash,role) KEY(user_id) VALUES(?,?,?,?)";
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, user.getUserId().toString());
                        ps.setString(2, user.getEmail());
                        ps.setString(3, user.getPasswordHash());
                        ps.setString(4, user.getRole().name());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Failed to persist user: " + user.getEmail(), e);
                }
        }
}
