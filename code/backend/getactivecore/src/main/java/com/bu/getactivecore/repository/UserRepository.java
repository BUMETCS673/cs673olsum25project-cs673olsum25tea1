package com.bu.getactivecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bu.getactivecore.model.users.Users;

/**
 * Repository interface for managing {@link Users} entities.
 */
public interface UserRepository extends JpaRepository<Users, String> {

	Optional<Users> findByUsername(String username);

	Optional<Users> findByEmail(String email);

	Optional<Users> findByEmailAndUsername(String email, String username);

}
