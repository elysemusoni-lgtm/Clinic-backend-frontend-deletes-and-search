package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kigali.clinic.rw.domain.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    List<Doctor> findByFirstNameIgnoreCaseOrderByLastNameAsc(String firstName);

    List<Doctor> findDistinctBySpecializations_NameIgnoreCaseOrderByLastNameAscFirstNameAsc(String name);

    List<Doctor> findByOfficeIsNullOrderByLastNameAsc();

    boolean existsByOffice_OfficeNumberAndIdNot(int officeNumber, UUID doctorId);
}
