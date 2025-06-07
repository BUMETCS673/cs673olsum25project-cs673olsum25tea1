package com.bu.getactivecore.service.activity.entity;

import com.bu.getactivecore.model.activity.Activity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDateTime;

/**
 * The DTO for creating a new activity.
 */
@Value
@Builder
public class ActivityCreateRequestDto {

    @NotBlank(message = "Name cannot be blank")
    @Size.List({
            @Size(max = 250, message = "The length of name must be less or equal to 250")
    })
    private String name;

    @Size(max = 250, message = "The length of description must be greater or equal to 1")
    private String description;

    @NotBlank(message = "Location cannot be blank")
    @Size.List({
            @Size(max = 250, message = "The length of location must be less or equal to 250")
    })
    private String location;

    @NotNull(message = "Start DateTime cannot be blank")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDateTime;

    @NotNull(message = "Start DateTime cannot be blank")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDateTime;

    public static Activity from(ActivityCreateRequestDto request) {
        return Activity.builder()
                .location(request.getLocation())
                .name(request.getName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .description(request.getDescription())
                .build();
    }
}
