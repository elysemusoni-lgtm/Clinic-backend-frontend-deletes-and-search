package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<List<Appointment>> getUpcomingAppointments(String ssNumber) {
        if (!patientRepository.existsBySsNumber(ssNumber)) {
            return Optional.empty();
        } else {
            return Optional.of(appointmentRepository
                    .findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                            ssNumber,
                            Date.valueOf(LocalDate.now()),
                            List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)));
        }
    }

    public List<Patient> getFrequentPatients(int minAppointments) {
        return patientRepository.findFrequentPatients(minAppointments);
    }

    public List<Patient> getPatientsOfDoctor(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "The doctor with that id does not exist");
        }
        return patientRepository.findDistinctByAppointmentDoctorId(doctorId);
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
        patient.setSsNumber(newData.getSsNumber());
        patient.setDateOfBirth(newData.getDateOfBirth());
        return patientRepository.save(patient);
    }

    
    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
