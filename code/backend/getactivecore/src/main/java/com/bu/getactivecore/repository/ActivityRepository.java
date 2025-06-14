package com.bu.getactivecore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bu.getactivecore.model.activity.Activity;

/**
 * Repository interface for Activity entity.
 */
public interface ActivityRepository extends JpaRepository<Activity, String> {
	Page<Activity> findByNameContaining(String name, Pageable pageable);

	Optional<Activity> findByName(String name);
}
