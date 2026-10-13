package kigali.clinic.rw.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        List<UUID> specializationIds = getSpecializationIds(doctor.getSpecializations());
        doctor.setOffice(findOffice(doctor.getOffice()));
        doctor.setSpecializations(new ArrayList<>());
        Doctor savedDoctor = doctorRepository.save(doctor);
        associateWithSpecializations(savedDoctor, specializationIds);
        return savedDoctor;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<List<Appointment>> getDoctorAgenda(UUID doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            return Optional.empty();
        } else {
            return Optional.of(appointmentRepository
                    .findByDoctor_IdAndAppointmentDateAndStatusNot(
                            doctorId, Date.valueOf(date), AppointmentStatus.CANCELLED));
        }
    }

    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepository.findByOfficeIsNullOrderByLastNameAsc();
    }

    public List<Doctor> findDoctorsByLastName(String lastName) {
        return doctorRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public List<Doctor> findDoctorsByFirstName(String firstName) {
        return doctorRepository.findByFirstNameIgnoreCaseOrderByLastNameAsc(firstName);
    }

    public List<Doctor> findDoctorsBySpecializationName(String name) {
        return doctorRepository.findDistinctBySpecializations_NameIgnoreCaseOrderByLastNameAscFirstNameAsc(name);
    }

    public Doctor getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    @Transactional
    public Doctor updateDoctor(UUID id, Doctor newData) {
        Doctor doctor = getDoctorById(id);
        List<UUID> specializationIds = getSpecializationIds(newData.getSpecializations());
        doctor.setFirstName(newData.getFirstName());
        doctor.setLastName(newData.getLastName());
        doctor.setDateOfBirth(newData.getDateOfBirth());
        if (newData.getOffice() != null) {
            doctor.setOffice(findOffice(newData.getOffice()));
        }
        Doctor savedDoctor = doctorRepository.save(doctor);
        associateWithSpecializations(savedDoctor, specializationIds);
        return savedDoctor;
    }

    @Transactional
    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Specialization specialization = specializationRepository.findById(specializationId).orElse(null);
        if (specialization == null) {
            return "That specialization does not exist";
        }

        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        specialization.getDoctors().add(doctor);
        doctorRepository.save(doctor);
        return "Specialization added to the doctor";
    }

    @Transactional
    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Specialization specialization = specializationRepository.findById(specializationId).orElse(null);
        if (specialization == null) {
            return "That specialization does not exist";
        }

        boolean removed = doctor.getSpecializations().removeIf(
                existing -> existing.getId().equals(specializationId));
        if (!removed) {
            return "The doctor does not have that specialization";
        }

        specialization.getDoctors().removeIf(
                existing -> existing.getId().equals(doctorId));
        doctorRepository.save(doctor);
        return "Specialization removed from the doctor";
    }

    @Transactional
    public String moveDoctorToOffice(UUID doctorId, int officeNumber) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Office office = officeRepository.findByOfficeNumber(officeNumber).orElse(null);
        if (office == null) {
            return "The office with that office number does not exist";
        }

        if (doctorRepository.existsByOffice_OfficeNumberAndIdNot(officeNumber, doctorId)) {
            return "That office is taken by other doctor";
        }

        doctor.setOffice(office);
        doctorRepository.save(doctor);
        return "Office changed successfully";
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        Doctor doctor = getDoctorById(id);
        for (Specialization specialization : doctor.getSpecializations()) {
            specialization.getDoctors().removeIf(linkedDoctor -> linkedDoctor.getId().equals(id));
        }
        doctor.getSpecializations().clear();
        doctorRepository.save(doctor);
        doctorRepository.deleteById(id);
    }

    private List<UUID> getSpecializationIds(List<Specialization> specializations) {
        if (specializations == null) {
            return List.of();
        }
        return specializations.stream()
                .map(specialization -> {
                    if (specialization == null || specialization.getId() == null) {
                        throw new IllegalArgumentException("Each specialization must have an id");
                    }
                    return specialization.getId();
                })
                .distinct()
                .toList();
    }

    private Office findOffice(Office office) {
        if (office == null) {
            return null;
        }
        if (office.getId() == null) {
            throw new IllegalArgumentException("Office must have an id");
        }
        return officeRepository.findById(office.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Office not found with id: " + office.getId()));
    }

    private void associateWithSpecializations(Doctor doctor, List<UUID> specializationIds) {
        List<Specialization> selected = specializationRepository.findAllById(specializationIds);
        if (selected.size() != specializationIds.size()) {
            throw new IllegalArgumentException("One or more specializations were not found");
        }

        for (Specialization specialization : doctor.getSpecializations()) {
            if (!specializationIds.contains(specialization.getId())) {
                specialization.getDoctors().removeIf(linkedDoctor -> linkedDoctor.getId().equals(doctor.getId()));
            }
        }
        for (Specialization specialization : selected) {
            if (specialization.getDoctors().stream()
                    .noneMatch(linkedDoctor -> linkedDoctor.getId().equals(doctor.getId()))) {
                specialization.getDoctors().add(doctor);
            }
        }
        doctor.setSpecializations(selected);
        doctorRepository.save(doctor);
    }
}
