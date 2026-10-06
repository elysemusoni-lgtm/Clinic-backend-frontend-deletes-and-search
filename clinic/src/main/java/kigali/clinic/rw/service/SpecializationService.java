package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    public Specialization createSpecialization(Specialization specialization) {
        List<UUID> doctorIds = getDoctorIds(specialization.getDoctors());
        specialization.setDoctors(findDoctors(doctorIds));
        return specializationRepository.save(specialization);
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    public Specialization getSpecializationById(UUID id) {
        return specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found with id: " + id));
    }

    @Transactional
    public Specialization updateSpecialization(UUID id, Specialization newData) {
        Specialization specialization = getSpecializationById(id);
        specialization.setName(newData.getName());
        specialization.setDoctors(findDoctors(getDoctorIds(newData.getDoctors())));
        return specializationRepository.save(specialization);
    }

    public void deleteSpecialization(UUID id) {
        specializationRepository.deleteById(id);
    }

    private List<UUID> getDoctorIds(List<Doctor> doctors) {
        if (doctors == null) {
            return List.of();
        }
        return doctors.stream()
                .map(doctor -> {
                    if (doctor == null || doctor.getId() == null) {
                        throw new IllegalArgumentException("Each doctor must have an id");
                    }
                    return doctor.getId();
                })
                .distinct()
                .toList();
    }

    private List<Doctor> findDoctors(List<UUID> doctorIds) {
        List<Doctor> doctors = doctorRepository.findAllById(doctorIds);
        if (doctors.size() != doctorIds.size()) {
            throw new IllegalArgumentException("One or more doctors were not found");
        }
        return new ArrayList<>(doctors);
    }
}
