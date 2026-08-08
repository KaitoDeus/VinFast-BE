package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.audit.AuditLogService;

import com.oem.evwarranty.domain.claim.Appointment;
import com.oem.evwarranty.domain.claim.AppointmentRepository;
import com.oem.evwarranty.common.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    public AppointmentService(AppointmentRepository appointmentRepository, AuditLogService auditLogService) {
        this.appointmentRepository = appointmentRepository;
        this.auditLogService = auditLogService;
    }

    public Appointment schedule(@NonNull Appointment appointment) {
        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log("SYSTEM", "SCHEDULE", "APPOINTMENT", saved.getId(),
                "Scheduled for vehicle: " + appointment.getVehicle().getVin());
        return saved;
    }

    public List<Appointment> getForServiceCenter(String sc) {
        return appointmentRepository.findByServiceCenterOrderByAppointmentDateAsc(sc);
    }

    public Appointment updateStatus(@NonNull Long id, Appointment.AppointmentStatus status) {
        return appointmentRepository.findById(id)
                .map(a -> {
                    a.setStatus(status);
                    auditLogService.log("SYSTEM", "UPDATE_STATUS", "APPOINTMENT", id, "Status updated to " + status);
                    return appointmentRepository.save(a);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }
}



