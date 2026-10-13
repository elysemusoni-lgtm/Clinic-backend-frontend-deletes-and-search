package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository.DoctorAppointmentCount;
import kigali.clinic.rw.repository.AppointmentRepository.PastPendingAppointment;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/save")
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        String message = appointmentService.validateAppointment(appointment);
        if (message.equals("Appointment date cannot be in the past")
                || message.equals("Appointment date is required")
                || message.equals("An existing patient id is required")
                || message.equals("An existing doctor id is required")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.startsWith("Patient not found with id:")
                || message.startsWith("Doctor not found with id:")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("Doctor already booked")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(appointmentService.createAppointment(appointment));
        }
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<String> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @RequestParam AppointmentStatus status) {
        String message = appointmentService.updateAppointmentStatus(appointmentId, status);
        if (message.startsWith("Cannot change status from ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @PatchMapping("/{appointmentId}/reschedule")
    public ResponseEntity<String> rescheduleAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam LocalDate newDate) {
        String message = appointmentService.rescheduleAppointment(appointmentId, newDate);
        if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("A COMPLETED appointment cannot be rescheduled")
                || message.equals("A CANCELLED appointment cannot be rescheduled")
                || message.equals("Appointment date cannot be in the past")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.equals("Doctor is already booked on that date")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @GetMapping("/by-status")
    public List<Appointment> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    @PatchMapping("/cancel-day")
    public String cancelAppointmentsForDoctorAndDate(
            @RequestParam UUID doctorId,
            @RequestParam LocalDate date) {
        int cancelledCount = appointmentService.cancelDoctorAppointmentsForDate(doctorId, date);
        return cancelledCount + " appointments cancelled";
    }

    @GetMapping("/stats/by-status")
    public List<Object[]> getAppointmentCountsByStatus() {
        return appointmentService.getAppointmentCountsByStatus();
    }

    @GetMapping("/between")
    public ResponseEntity<?> getAppointmentsBetween(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        if (start.isAfter(end)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("start must be on or before end");
        } else {
            return ResponseEntity.ok(appointmentService.getAppointmentsBetween(start, end));
        }
    }

    @GetMapping("/doctors/{doctorId}/scheduled")
    public List<Appointment> getScheduledAppointmentsForDoctor(@PathVariable UUID doctorId) {
        return appointmentService.getScheduledAppointmentsForDoctor(doctorId);
    }

    @GetMapping("/pending/past")
    public List<PastPendingAppointment> getPastPendingAppointments() {
        return appointmentService.getPastPendingAppointments();
    }

    @GetMapping("/appointment-counts-by-doctor")
    public List<DoctorAppointmentCount> getAppointmentCountsByDoctor() {
        return appointmentService.getAppointmentCountsByDoctor();
    }

    @GetMapping("/specializations/{specializationId}")
    public List<Appointment> getAppointmentsBySpecialization(@PathVariable UUID specializationId) {
        return appointmentService.getAppointmentsBySpecialization(specializationId);
    }

    @GetMapping("/doctors/{doctorId}/page")
    public Page<Appointment> getDoctorAppointments(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return appointmentService.getDoctorAppointments(doctorId, page, size);
    }
}
