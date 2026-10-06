package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.AppointmentRepository.DoctorAppointmentCount;
import kigali.clinic.rw.repository.AppointmentRepository.PastPendingAppointment;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new IllegalArgumentException("An existing patient id is required");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            throw new IllegalArgumentException("An existing doctor id is required");
        }

        UUID patientId = appointment.getPatient().getId();
        UUID doctorId = appointment.getDoctor().getId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + doctorId));

        if (appointmentRepository.existsActiveAppointmentForDoctorOnDate(
                doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Doctor already booked");
        }

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getScheduledAppointmentsForDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctor_IdAndStatusOrderByAppointmentDateAsc(
                doctorId, AppointmentStatus.SCHEDULED);
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status);
    }

    @Transactional
    public int cancelDoctorAppointmentsForDate(UUID doctorId, LocalDate date) {
        return appointmentRepository.cancelDoctorAppointmentsForDate(
                doctorId,
                Date.valueOf(date),
                AppointmentStatus.CANCELLED,
                AppointmentStatus.COMPLETED);
    }

    public List<Object[]> getAppointmentCountsByStatus() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(
                Date.valueOf(start), Date.valueOf(end));
    }

    public List<PastPendingAppointment> getPastPendingAppointments() {
        return appointmentRepository.findPastAppointmentsByStatus(AppointmentStatus.PENDING);
    }

    public List<DoctorAppointmentCount> getAppointmentCountsByDoctor() {
        return appointmentRepository.countAppointmentsByDoctor();
    }

    public List<Appointment> getAppointmentsBySpecialization(UUID specializationId) {
        return appointmentRepository.findDistinctByDoctor_Specializations_Id(specializationId);
    }

    public Page<Appointment> getDoctorAppointments(UUID doctorId, int page, int size) {
        return appointmentRepository.findByDoctor_IdOrderByAppointmentDateAsc(
                doctorId, PageRequest.of(page, size));
    }
}
