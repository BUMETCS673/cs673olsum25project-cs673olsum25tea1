package com.bu.getactivecore.model.activity;

import org.hibernate.annotations.UuidGenerator;

import com.bu.getactivecore.model.users.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Purpose of this class is to provide a fast lookup of users and their
 * activities.
 */
@Entity
@Table(name = "user_activities", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
		"activity_id" }), indexes = {
				@Index(name = "idx_user_activities_userid_activityid", columnList = "user_id, activity_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {

	@Id
	@UuidGenerator
	private String id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private Users user;

	@ManyToOne
	@JoinColumn(name = "activity_id", referencedColumnName = "id")
	private Activity activity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoleType role;
}