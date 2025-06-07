package com.bu.getactivecore.service.activity.entity;

import jakarta.validation.constraints.NotBlank;

import lombok.Value;

@Value
public class ActivityParticipantRequestDto {

    @NotBlank(message = "Activity ID cannot be blank")
    private String activityId;
}