package com.potens.schemerecommender.repository;

import com.potens.schemerecommender.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, Long> {

    /**
     * Fetch all active schemes with rules in a single query (N+1 safe).
     * Used by the recommendation engine.
     */
    @Query("SELECT DISTINCT s FROM Scheme s LEFT JOIN FETCH s.rules WHERE s.isActive = true")
    List<Scheme> findAllActiveWithRules();

    /**
     * Fetch a single scheme with its rules (N+1 safe).
     * Used by getSchemeById, updateScheme, explainScheme.
     */
    @Query("SELECT s FROM Scheme s LEFT JOIN FETCH s.rules WHERE s.id = :id")
    Optional<Scheme> findByIdWithRules(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
