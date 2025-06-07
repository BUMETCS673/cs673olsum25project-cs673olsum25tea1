package com.bu.getactivecore.model.users;

import com.bu.getactivecore.service.users.entity.UserDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Custom implementation of the {@link UserDetails} interface used by Spring Security.
 *
 * <p>This class acts as an adapter between the application's {@link Users} entity and the Spring Security framework.
 * It wraps a {@link UserDto} instance and provides the necessary user information (e.g., username, password, roles)
 * required for authentication and authorization processes.</p>
 *
 * <p>By implementing {@code UserDetails}, this class enables Spring Security to understand how to retrieve
 * user credentials and authorities for security checks during login and request authorization.</p>
 *
 * <p>Currently, each user is assigned a default authority role of <strong>"USER"</strong>.</p>
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final UserDto userDto;

    private final Collection<? extends GrantedAuthority> m_authorities;

    /**
     * Constructor that initializes the UserPrincipal with a {@link UserDto} entity.
     *
     * @param userDto the {@link UserDto} entity representing the user
     */
    public UserPrincipal(UserDto userDto) {
        this.userDto = userDto;
        m_authorities = Set.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(userDto.getAccountState().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return m_authorities;
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return userDto.getUsername();
    }
}
