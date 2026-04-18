package com.healthcare.pattern.behavioral;

import com.healthcare.model.Appointment;
import com.healthcare.repository.IApptRepository;

import java.util.UUID;

public class CancelAppointmentCommand implements Command {
    private final IApptRepository repo;
    private final UUID appointmentId;
    private Appointment snapshot;

    public CancelAppointmentCommand(IApptRepository repo, UUID appointmentId) {
        this.repo = repo;
        this.appointmentId = appointmentId;
    }

    @Override
    public void execute() {
        snapshot = repo.findById(appointmentId);
        if (snapshot != null) { snapshot.cancel(); repo.update(snapshot); }
    }

    @Override
    public void undo() {
        if (snapshot != null) { repo.update(snapshot); } // restore previous state
    }
}
