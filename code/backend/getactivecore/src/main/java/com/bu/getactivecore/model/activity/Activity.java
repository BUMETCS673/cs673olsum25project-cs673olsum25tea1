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
@Entity(name = "activities")
@Table(name = "activities")
public class Activity {
	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "location")
	private String location;

	@Column(name = "start_date_time")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime startDateTime;

	@Column(name = "end_date_time")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime endDateTime;
}
