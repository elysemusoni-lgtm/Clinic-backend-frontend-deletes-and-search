package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctor_IdAndStatusOrderByAppointmentDateAsc(
            UUID doctorId, AppointmentStatus status);

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(
            java.sql.Date start, java.sql.Date end);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate = :appointmentDate
              AND (a.status IS NULL OR a.status <> :excludedStatus)
            """)
    boolean existsActiveAppointmentForDoctorOnDate(
            @Param("doctorId") UUID doctorId,
            @Param("appointmentDate") java.sql.Date appointmentDate,
            @Param("excludedStatus") AppointmentStatus excludedStatus);

    @Query("""
            SELECT CONCAT(p.firstName, ' ', p.lastName) AS patientName,
                   CONCAT(d.firstName, ' ', d.lastName) AS doctorName,
                   a.appointmentDate AS appointmentDate
            FROM Appointment a
            JOIN a.patient p
            JOIN a.doctor d
            WHERE a.status = :status AND a.appointmentDate < CURRENT_DATE
            ORDER BY a.appointmentDate ASC
            """)
    List<PastPendingAppointment> findPastAppointmentsByStatus(
            @Param("status") AppointmentStatus status);

    @Query("""
            SELECT d.id AS doctorId,
                   CONCAT(d.firstName, ' ', d.lastName) AS doctorName,
                   COUNT(a) AS appointmentCount
            FROM Doctor d
            LEFT JOIN d.appointments a
            GROUP BY d.id, d.firstName, d.lastName
            ORDER BY COUNT(a) DESC
            """)
    List<DoctorAppointmentCount> countAppointmentsByDoctor();

    List<Appointment> findDistinctByDoctor_Specializations_Id(UUID specializationId);

    Page<Appointment> findByDoctor_IdOrderByAppointmentDateAsc(UUID doctorId, Pageable pageable);

    interface PastPendingAppointment {
        String getPatientName();

        String getDoctorName();

        java.sql.Date getAppointmentDate();
    }

    interface DoctorAppointmentCount {
        UUID getDoctorId();

        String getDoctorName();

        Long getAppointmentCount();
    }
}
