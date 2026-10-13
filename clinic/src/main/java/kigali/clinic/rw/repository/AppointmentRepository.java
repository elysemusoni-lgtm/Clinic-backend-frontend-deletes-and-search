package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctor_IdAndStatusOrderByAppointmentDateAsc(
            UUID doctorId, AppointmentStatus status);

    List<Appointment> findByDoctor_IdAndAppointmentDateAndStatusNot(
            UUID doctorId, java.sql.Date appointmentDate, AppointmentStatus status);

    List<Appointment> findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            String ssNumber,
            java.sql.Date appointmentDate,
            List<AppointmentStatus> statuses);

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    @Query("""
            SELECT a.status, COUNT(a)
            FROM Appointment a
            WHERE a.status IS NOT NULL
            GROUP BY a.status
            """)
    List<Object[]> countAppointmentsByStatus();

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(
            java.sql.Date start, java.sql.Date end);

    boolean existsByDoctor_IdAndAppointmentDateAndStatusNotAndIdNot(
            UUID doctorId,
            java.sql.Date appointmentDate,
            AppointmentStatus status,
            UUID appointmentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            UPDATE Appointment a
            SET a.status = :cancelledStatus
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate = :appointmentDate
              AND a.status IN (:scheduledStatus, :confirmedStatus)
            """)
    int cancelDoctorAppointmentsForDate(
            @Param("doctorId") UUID doctorId,
            @Param("appointmentDate") java.sql.Date appointmentDate,
            @Param("scheduledStatus") AppointmentStatus scheduledStatus,
            @Param("confirmedStatus") AppointmentStatus confirmedStatus,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus);

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

    @Query("""
            SELECT o.name AS officeName,
                   o.officeNumber AS officeNumber,
                   COUNT(a) AS appointmentCount
            FROM Appointment a
            JOIN a.doctor d
            JOIN d.office o
            GROUP BY o.id, o.name, o.officeNumber
            ORDER BY COUNT(a) DESC, o.officeNumber ASC
            """)
    List<BusiestOffice> findOfficesByAppointmentCount();

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

    interface BusiestOffice {
        String getOfficeName();

        Integer getOfficeNumber();

        Long getAppointmentCount();
    }
}
