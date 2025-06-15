package com.bu.getactivecore.service.activity.entity;

import java.time.LocalDateTime;

import com.bu.getactivecore.model.activity.ActivityComment;

import lombok.Builder;
import lombok.Data;

/**
 * Activity Comment DTO for exposing activity comment data.
 */
@Data
@Builder
public class ActivityCommentDto {

	private String id;

	private String userId;

	private String activityId;

	private String comment;

	private LocalDateTime timestamp;

	/**
	 * Converts an Activity Comment entity to an ActivityCommentDto.
	 *
	 * @param activity the Activity entity
	 * @return the ActivityDto
	 */
	public static ActivityCommentDto of(ActivityComment comment) {
		return ActivityCommentDto.builder().id(comment.getId()).userId(comment.getUserId())
				.activityId(comment.getActivityId()).comment(comment.getComment()).timestamp(comment.getTimestamp())
				.build();
	}

}
