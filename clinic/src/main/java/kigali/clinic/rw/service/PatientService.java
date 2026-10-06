package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> findPatientsByLastName(String lastName) {
        return patientRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public List<Patient> findPatientsByFirstName(String firstName) {
        return patientRepository.findByFirstNameIgnoreCaseOrderByLastNameAsc(firstName);
    }

   
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    
    public Patient updatePatient(UUID id, Patient newData) {
        Patient patient = getPatientById(id);
        patient.setFirstName(newData.getFirstName());
        patient.setLastName(newData.getLastName());
        patient.setDateOfBirth(newData.getDateOfBirth());
        return patientRepository.save(patient);
    }

    
    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
