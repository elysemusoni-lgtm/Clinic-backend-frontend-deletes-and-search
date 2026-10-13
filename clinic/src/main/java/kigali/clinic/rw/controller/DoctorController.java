package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // CREATE  -> POST /api/doctors
    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.createDoctor(doctor);
    }

    // READ all -> GET /api/doctors
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/search")
    public List<Doctor> searchDoctorsByName(@RequestParam String name) {
        return doctorService.searchDoctorsByName(name);
    }

    @GetMapping("/workload")
    public List<Object[]> getDoctorWorkload(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return doctorService.getDoctorWorkload(start, end);
    }

    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> getDoctorAgenda(
            @PathVariable UUID doctorId,
            @RequestParam LocalDate date) {
        Optional<List<Appointment>> appointments = doctorService.getDoctorAgenda(doctorId, date);
        if (appointments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The doctor with that id does not exist");
        } else {
            return ResponseEntity.ok(appointments.get());
        }
    }

    @GetMapping("/without-office")
    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorService.getDoctorsWithoutOffice();
    }

    @GetMapping("/by-last-name")
    public List<Doctor> findDoctorsByLastName(@RequestParam String lastName) {
        return doctorService.findDoctorsByLastName(lastName);
    }

    @GetMapping("/by-first-name")
    public List<Doctor> findDoctorsByFirstName(@RequestParam String firstName) {
        return doctorService.findDoctorsByFirstName(firstName);
    }

    @GetMapping("/by-specialization")
    public List<Doctor> findDoctorsBySpecialization(@RequestParam String name) {
        return doctorService.findDoctorsBySpecializationName(name);
    }

    // READ one -> GET /api/doctors/{id}
    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable UUID id) {
        return doctorService.getDoctorById(id);
    }

    // UPDATE -> PUT /api/doctors/{id}
    @PutMapping("/{id}")
    public Doctor updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor) {
        return doctorService.updateDoctor(id, doctor);
    }

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> addSpecialization(
            @PathVariable UUID doctorId,
            @PathVariable UUID specializationId) {
        String message = doctorService.addSpecialization(doctorId, specializationId);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The doctor already has that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> removeSpecialization(
            @PathVariable UUID doctorId,
            @PathVariable UUID specializationId) {
        String message = doctorService.removeSpecialization(doctorId, specializationId);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The doctor does not have that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @PatchMapping("/{doctorId}/office")
    public ResponseEntity<String> moveDoctorToOffice(
            @PathVariable UUID doctorId,
            @RequestParam int officeNumber) {
        String message = doctorService.moveDoctorToOffice(doctorId, officeNumber);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("The office with that office number does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That office is taken by other doctor")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    // DELETE -> DELETE /api/doctors/{id}
    @DeleteMapping("/{id}")
    public void deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
    }
}
