package com.bu.getactivecore.service.activity.entity;

import lombok.Builder;
import lombok.Value;

/**
 * DTO for deleting an activity.
 */
@Value
@Builder
public class ActivityDeleteRequestDto {

    /**
     * If true, the activity will be forcefully deleted even if it has participants.
     */
	boolean force;

}
