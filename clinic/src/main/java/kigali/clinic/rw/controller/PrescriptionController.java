package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<?> createPrescription(@RequestBody Prescription prescription) {
        if (prescription.getAppointment() == null || prescription.getAppointment().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("An existing appointment id is required");
        } else {
            Optional<Prescription> savedPrescription = prescriptionService.createPrescription(prescription);
            if (savedPrescription.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The appointment with that id does not exist");
            } else {
                return ResponseEntity.ok(savedPrescription.get());
            }
        }
    }

    @GetMapping
    public List<Prescription> getAllPrescriptions() {
        return prescriptionService.getAllPrescriptions();
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> getPrescriptionsForAppointment(@PathVariable UUID appointmentId) {
        return prescriptionService.getPrescriptionsForAppointment(appointmentId);
    }
}
