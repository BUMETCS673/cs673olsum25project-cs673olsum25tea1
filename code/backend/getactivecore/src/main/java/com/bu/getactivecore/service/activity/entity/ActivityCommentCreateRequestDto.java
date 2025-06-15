package com.bu.getactivecore.service.activity.entity;

import com.bu.getactivecore.model.activity.ActivityComment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * The DTO for creating a new activity comment.
 */
@Value
@Builder
public class ActivityCommentCreateRequestDto {

	@NotBlank(message = "comment cannot be blank")
	@Size(max = 250, message = "The length of comment must be less or equal to 250")
	private String comment;

	public static ActivityComment from(ActivityCommentCreateRequestDto request) {
		return ActivityComment.builder().comment(request.getComment()).build();
	}

}
