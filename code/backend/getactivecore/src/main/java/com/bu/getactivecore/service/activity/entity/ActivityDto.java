package com.bu.getactivecore.service.activity.entity;

import com.bu.getactivecore.model.activity.Activity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Activity DTO for exposing activity data.
 */
@Data
@Builder
public class ActivityDto {

    private String id;

    private String name;

    private String description;

    private String location;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    /**
     * Converts an Activity entity to an ActivityDto.
     *
     * @param activity the Activity entity
     * @return the ActivityDto
     */
    public static ActivityDto of(Activity activity) {
        return ActivityDto.builder()
                .id(activity.getId())
                .location(activity.getLocation())
                .name(activity.getName())
                .startDateTime(activity.getStartDateTime())
                .endDateTime(activity.getEndDateTime())
                .description(activity.getDescription())
                .build();
    }
}
