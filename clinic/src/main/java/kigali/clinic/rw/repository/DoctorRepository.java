package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kigali.clinic.rw.domain.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    @Query("""
            SELECT d.firstName, d.lastName, COUNT(a)
            FROM Doctor d
            JOIN d.appointments a
            WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.COMPLETED
              AND a.appointmentDate BETWEEN :start AND :end
            GROUP BY d.id, d.firstName, d.lastName
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> findDoctorWorkload(
            @Param("start") java.sql.Date start,
            @Param("end") java.sql.Date end);

    @Query("""
            SELECT d
            FROM Doctor d
            WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY d.lastName ASC
            """)
    List<Doctor> searchByName(@Param("name") String name);

    List<Doctor> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    List<Doctor> findByFirstNameIgnoreCaseOrderByLastNameAsc(String firstName);

    List<Doctor> findDistinctBySpecializations_NameIgnoreCaseOrderByLastNameAscFirstNameAsc(String name);

    List<Doctor> findByOfficeIsNullOrderByLastNameAsc();

    boolean existsByOffice_OfficeNumberAndIdNot(int officeNumber, UUID doctorId);
}
