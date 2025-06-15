package com.bu.getactivecore.model.users;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uc_users_email", columnNames = { "email" }),
		@UniqueConstraint(name = "uc_users_username", columnNames = { "username" }) })
public class Users {
	@Id
	@UuidGenerator
	private String userId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_state", nullable = false)
    @Builder.Default
	private AccountState accountState = AccountState.UNVERIFIED;

    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "avatar_updated_at")
    private LocalDateTime avatarUpdatedAt;
}
