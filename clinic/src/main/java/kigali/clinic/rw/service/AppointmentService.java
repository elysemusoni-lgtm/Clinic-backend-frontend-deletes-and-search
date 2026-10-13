package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        UUID patientId = appointment.getPatient().getId();
        UUID doctorId = appointment.getDoctor().getId();
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    public String validateAppointment(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }
        if (appointment.getAppointmentDate().toLocalDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "An existing patient id is required";
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "An existing doctor id is required";
        }
        UUID patientId = appointment.getPatient().getId();
        if (!patientRepository.existsById(patientId)) {
            return "Patient not found with id: " + patientId;
        }
        UUID doctorId = appointment.getDoctor().getId();
        if (!doctorRepository.existsById(doctorId)) {
            return "Doctor not found with id: " + doctorId;
        }
        if (appointmentRepository.existsActiveAppointmentForDoctorOnDate(
                doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            return "Doctor already booked";
        }
        return "";
    }

    @Transactional
    public String updateAppointmentStatus(UUID appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }

        AppointmentStatus currentStatus = appointment.getStatus();
        boolean allowed = (currentStatus == AppointmentStatus.SCHEDULED
                    && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED))
                || (currentStatus == AppointmentStatus.CONFIRMED
                    && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED));
        if (!allowed) {
            return "Cannot change status from " + currentStatus + " to " + newStatus;
        }

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        return "Appointment status updated successfully";
    }

    @Transactional
    public String rescheduleAppointment(UUID appointmentId, LocalDate newDate) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return "A COMPLETED appointment cannot be rescheduled";
        } else if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A CANCELLED appointment cannot be rescheduled";
        } else if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        } else if (appointmentRepository.existsByDoctor_IdAndAppointmentDateAndStatusNotAndIdNot(
                appointment.getDoctor().getId(),
                Date.valueOf(newDate),
                AppointmentStatus.CANCELLED,
                appointmentId)) {
            return "Doctor is already booked on that date";
        } else {
            appointment.setAppointmentDate(Date.valueOf(newDate));
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointmentRepository.save(appointment);
            return "Appointment rescheduled successfully";
        }
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
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.CANCELLED);
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
