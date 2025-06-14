package com.bu.getactivecore.service.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.users.entity.UserDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for retrieving user information during authentication to
 * convert {@link Users} entity into a {@link UserPrincipal} to be used by
 * Spring Security.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository m_userRepo;

	/**
	 * Constructs the service with the provided {@link UserRepository}.
	 *
	 * @param userRepo used to fetch user data
	 */
	public CustomUserDetailsService(UserRepository userRepo) {
		m_userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<Users> user = m_userRepo.findByUsername(username);
		if (user.isEmpty()) {
			log.warn("User not found with username: {}", username);
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return new UserPrincipal(UserDto.of(user.get()));
	}
}
