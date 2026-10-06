package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.service.PatientService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // CREATE  -> POST /api/patients
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    // READ all -> GET /api/patients
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/frequent")
    public List<Patient> getFrequentPatients(@RequestParam int min) {
        return patientService.getFrequentPatients(min);
    }

    @GetMapping("/of-doctor/{doctorId}")
    public List<Patient> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        return patientService.getPatientsOfDoctor(doctorId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handlePatientStatusException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
    }

    @GetMapping("/by-last-name")
    public List<Patient> findPatientsByLastName(@RequestParam String lastName) {
        return patientService.findPatientsByLastName(lastName);
    }

    @GetMapping("/by-first-name")
    public List<Patient> findPatientsByFirstName(@RequestParam String firstName) {
        return patientService.findPatientsByFirstName(firstName);
    }

    // READ one -> GET /api/patients/{id}
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable UUID id) {
        return patientService.getPatientById(id);
    }

    // UPDATE -> PUT /api/patients/{id}
    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        return patientService.updatePatient(id, patient);
    }

    // DELETE -> DELETE /api/patients/{id}
    @DeleteMapping("/{id}")
    public void deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
    }
}
