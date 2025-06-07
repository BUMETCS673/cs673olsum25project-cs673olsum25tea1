package com.bu.getactivecore.service.activity.entity;

import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.model.activity.RoleType;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserActivityDto {
    private String id;

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
                .userId(userActivity.getUserId())
                .activityId(userActivity.getActivity().getId())
                .description(userActivity.getActivity().getDescription())
                .location(userActivity.getActivity().getLocation())
                .startDateTime(userActivity.getActivity().getStartDateTime())
                .endDateTime(userActivity.getActivity().getEndDateTime())
                .role(userActivity.getRole())
                .build();
    }

}
