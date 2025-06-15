package com.bu.getactivecore.model.activity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "activitycomments")
@Table(name = "activitycomments")
public class ActivityComment {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "activity_id", nullable = false)
	private String activityId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "comment", nullable = false)
	private String comment;

	@Column(name = "timestamp")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime timestamp;

}
