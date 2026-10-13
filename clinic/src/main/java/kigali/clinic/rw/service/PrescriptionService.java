package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public Optional<Prescription> createPrescription(Prescription prescription) {
        if (prescription.getAppointment() == null || prescription.getAppointment().getId() == null) {
            return Optional.empty();
        }
        UUID appointmentId = prescription.getAppointment().getId();
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isEmpty()) {
            return Optional.empty();
        }
        prescription.setAppointment(appointment.get());
        return Optional.of(prescriptionRepository.save(prescription));
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public List<Prescription> getPrescriptionsForAppointment(UUID appointmentId) {
        return prescriptionRepository.findByAppointment_IdOrderByIssuedDateDesc(appointmentId);
    }
}
