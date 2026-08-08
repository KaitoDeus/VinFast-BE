package com.oem.evwarranty.domain.claim;


import com.oem.evwarranty.domain.claim.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByServiceCenterOrderByAppointmentDateAsc(String serviceCenter);

    List<Appointment> findByVehicleIdOrderByAppointmentDateDesc(Long vehicleId);

    List<Appointment> findByCampaignId(Long campaignId);
}


