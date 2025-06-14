package com.bu.getactivecore.service.activity.entity;

import java.util.List;

import lombok.Value;

@Value
public class ActivityParticipantResponseDto {

	List<UserActivityDto> activities;
}
