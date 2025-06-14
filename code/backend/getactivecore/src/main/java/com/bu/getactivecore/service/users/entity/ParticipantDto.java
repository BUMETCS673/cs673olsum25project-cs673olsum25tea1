package com.bu.getactivecore.service.users.entity;

import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;

import lombok.Value;

/**
 * Represents a participant of an activity.
 */
@Value
public class ParticipantDto {

	/**
	 * The username of the user.
	 */
	String username;

	/**
	 * The type of participant in the activity.
	 */
	RoleType roleType;

	/**
	 * Creates a new instance of ParticipantDto from a UserActivity.
	 *
	 * @param userActivity the UserActivity to convert
	 * @return a new ParticipantDto instance
	 */
	public static ParticipantDto of(UserActivity userActivity) {
		return new ParticipantDto(userActivity.getUser().getUsername(), userActivity.getRole());
	}
}