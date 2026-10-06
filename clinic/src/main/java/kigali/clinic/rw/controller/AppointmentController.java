package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.createAppointment(appointment);
    }

    @GetMapping("/by-status")
    public List<Appointment> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    @GetMapping("/between")
    public List<Appointment> getAppointmentsBetween(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be on or before end");
        }
        return appointmentService.getAppointmentsBetween(start, end);
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
