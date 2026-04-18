package com.healthcare.pattern.structural;

import com.healthcare.model.*;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.service.*;

import java.util.UUID;

/**
 * FACADE PATTERN (Structural)
 * Member: Jayanth Reddy (PES1UG23CS264)
 * Single simplified interface into the subsystems: appointments, patients, prescriptions.
 */
public class ClinicFacade {
    private final IPatientService patientService;
    private final IAppointmentService appointmentService;
    private final IPrescriptionService prescriptionService;

    public ClinicFacade(IPatientService ps, IAppointmentService as, IPrescriptionService rxs) {
        this.patientService = ps;
        this.appointmentService = as;
        this.prescriptionService = rxs;
    }

    public Patient registerAndSchedule(PatientDTO dto, UUID clinicianId, String dateTime) {
        Patient p = patientService.register(dto);
        appointmentService.schedule(p.getUserId(), clinicianId, dateTime);
        return p;
    }

    public ConflictResult fullPrescriptionWorkflow(Appointment appt) {
        Prescription rx = prescriptionService.createRx(appt);
        return prescriptionService.checkConflicts(rx);
    }
}
