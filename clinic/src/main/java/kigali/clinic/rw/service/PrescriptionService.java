package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
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
    public String createPrescription(Prescription prescription, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        } else if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        } else {
            prescription.setAppointment(appointment);
            prescription.setIssuedDate(LocalDate.now());
            prescriptionRepository.save(prescription);
            return "Prescription saved successfully";
        }
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Optional<List<Prescription>> getPrescriptionsForAppointment(UUID appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            return Optional.empty();
        } else {
            return Optional.of(prescriptionRepository.findByAppointment_IdOrderByIssuedDateDesc(appointmentId));
        }
    }

    public List<Prescription> getPrescriptionsForPatient(String ssNumber) {
        return prescriptionRepository.findByPatientSsNumber(ssNumber);
    }

    @Transactional
    public String updatePrescription(UUID id, Prescription newData) {
        Prescription prescription = prescriptionRepository.findById(id).orElse(null);
        if (prescription == null) {
            return "The prescription with that id does not exist";
        } else {
            prescription.setMedicineName(newData.getMedicineName());
            prescription.setDosage(newData.getDosage());
            prescription.setDurationDays(newData.getDurationDays());
            prescriptionRepository.save(prescription);
            return "Prescription updated successfully";
        }
    }

    @Transactional
    public String deletePrescription(UUID id) {
        if (!prescriptionRepository.existsById(id)) {
            return "The prescription with that id does not exist";
        } else {
            prescriptionRepository.deleteById(id);
            return "Prescription deleted successfully";
        }
    }
}
