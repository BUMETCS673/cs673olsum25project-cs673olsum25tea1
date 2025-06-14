package com.bu.getactivecore.service.activity.entity;

import lombok.Builder;
import lombok.Value;

/**
 * DTO for deleting an activity.
 */
@Value
@Builder
public class ActivityDeleteRequestDto {

	// remove all participants from this activity
	private boolean force = false;

}
