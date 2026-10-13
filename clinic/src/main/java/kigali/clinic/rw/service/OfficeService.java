package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.AppointmentRepository.BusiestOffice;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    
    public Office createOffice(Office office) {
        return officeRepository.save(office);
    }

    
    public List<Office> getAllOffices() {
        return officeRepository.findAll();
    }

    public List<Office> getFreeOffices() {
        return officeRepository.findFreeOffices();
    }

    public Optional<BusiestOffice> getBusiestOffice() {
        return appointmentRepository.findOfficesByAppointmentCount().stream().findFirst();
    }

    public List<Office> findOfficesByNameStartingWith(String prefix) {
        return officeRepository.findByNameStartingWithIgnoreCase(prefix);
    }

    public List<Office> findOfficesByNameEndingWith(String suffix) {
        return officeRepository.findByNameEndingWithIgnoreCase(suffix);
    }

    
    public Office getOfficeById(UUID id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Office not found with id: " + id));
    }

    
    public Office updateOffice(UUID id, Office newData) {
        Office office = getOfficeById(id);
        office.setName(newData.getName());
        office.setOfficeNumber(newData.getOfficeNumber());
        return officeRepository.save(office);
    }

   
    public void deleteOffice(UUID id) {
        officeRepository.deleteById(id);
    }
}
