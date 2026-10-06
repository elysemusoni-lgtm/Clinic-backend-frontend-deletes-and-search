package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // DELETE -> DELETE /api/doctors/{id}
    @DeleteMapping("/{id}")
    public void deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
    }
}
