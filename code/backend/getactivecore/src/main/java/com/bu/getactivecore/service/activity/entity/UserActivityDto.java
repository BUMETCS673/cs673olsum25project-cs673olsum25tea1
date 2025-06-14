package com.bu.getactivecore.service.activity.entity;

import java.time.LocalDateTime;

import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserActivityDto {
    private String id;

    private String name;

    private String userId;

    private String activityId;

    private String description;

    private String location;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private RoleType role;

    public static UserActivityDto of(UserActivity userActivity) {
        return UserActivityDto.builder()
                .id(userActivity.getId())
				.userId(userActivity.getUser().getUserId())
                .name(userActivity.getActivity().getName())
                .activityId(userActivity.getActivity().getId())
                .description(userActivity.getActivity().getDescription())
                .location(userActivity.getActivity().getLocation())
                .startDateTime(userActivity.getActivity().getStartDateTime())
                .endDateTime(userActivity.getActivity().getEndDateTime())
                .role(userActivity.getRole())
                .build();
    }

}
