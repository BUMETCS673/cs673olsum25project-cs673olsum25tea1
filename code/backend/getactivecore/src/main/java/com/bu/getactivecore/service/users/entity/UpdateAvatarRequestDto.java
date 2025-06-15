package com.bu.getactivecore.service.users.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Value;

/**
 * DTO for avatar update request.
 */
@Value
public class UpdateAvatarRequestDto {
    /**
     * Base64 encoded image data.
     * Must start with "data:image/jpeg;base64," or "data:image/png;base64,"
     */
    @NotBlank(message = "Avatar data cannot be blank")
    @Pattern(
        regexp = "^data:image/(jpeg|png);base64,[A-Za-z0-9+/=]+$",
        message = "Invalid image format. Only JPEG and PNG are supported."
    )
    String avatarData;
} 