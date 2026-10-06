package kigali.clinic.rw.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kigali.clinic.rw.domain.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
}
