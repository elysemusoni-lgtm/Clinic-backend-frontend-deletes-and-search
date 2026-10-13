package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Office;

@Repository 
public interface OfficeRepository extends JpaRepository<Office,UUID> {
    
    List<Office> findByName(String name);
    List<Office> findByNameStartingWithIgnoreCase(String prefix);
    List<Office> findByNameEndingWithIgnoreCase(String suffix);
    Optional<Office> findByOfficeNumber(int officeN);

    @Query("""
            SELECT o
            FROM Office o
            LEFT JOIN o.doctor d
            WHERE d IS NULL
            ORDER BY o.officeNumber ASC
            """)
    List<Office> findFreeOffices();
}
