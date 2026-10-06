package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kigali.clinic.rw.domain.Patient;

// JpaRepository already gives us save(), findById(), findAll(), deleteById()
// so we don't need to write those ourselves.
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    List<Patient> findByFirstNameIgnoreCaseOrderByLastNameAsc(String firstName);

    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.doctor.id = :doctorId")
    List<Patient> findDistinctByAppointmentDoctorId(@Param("doctorId") UUID doctorId);
}
