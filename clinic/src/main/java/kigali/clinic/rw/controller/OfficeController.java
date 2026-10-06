package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.OfficeService;

@RestController
@RequestMapping("/api/offices")
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    // CREATE  -> POST /api/offices
    @PostMapping
    public Office createOffice(@RequestBody Office office) {
        return officeService.createOffice(office);
    }

    // READ all -> GET /api/offices
    @GetMapping
    public List<Office> getAllOffices() {
        return officeService.getAllOffices();
    }

    @GetMapping("/by-name/starting-with")
    public List<Office> findOfficesByNameStartingWith(@RequestParam String prefix) {
        return officeService.findOfficesByNameStartingWith(prefix);
    }

    @GetMapping("/by-name/ending-with")
    public List<Office> findOfficesByNameEndingWith(@RequestParam String suffix) {
        return officeService.findOfficesByNameEndingWith(suffix);
    }

    // READ one -> GET /api/offices/{id}
    @GetMapping("/{id}")
    public Office getOfficeById(@PathVariable UUID id) {
        return officeService.getOfficeById(id);
    }

    // UPDATE -> PUT /api/offices/{id}
    @PutMapping("/{id}")
    public Office updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        return officeService.updateOffice(id, office);
    }

    // DELETE -> DELETE /api/offices/{id}
    @DeleteMapping("/{id}")
    public void deleteOffice(@PathVariable UUID id) {
        officeService.deleteOffice(id);
    }
}
