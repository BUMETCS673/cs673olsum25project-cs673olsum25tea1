package com.bu.getactivecore.service.activity.entity;

import lombok.Value;

/**
 * The response DTO for an activity.
 * <p>
 * This DTO is used to return the details of an activity in the response.
 */
@Value
public class ActivityResponseDto {
	/**
	 * The activity details.
	 */
	ActivityDto activity;
}
