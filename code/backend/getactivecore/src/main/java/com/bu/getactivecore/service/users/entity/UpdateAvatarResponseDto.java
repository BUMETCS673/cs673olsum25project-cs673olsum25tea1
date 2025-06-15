package com.bu.getactivecore.service.users.entity;

import lombok.Value;
import java.time.LocalDateTime;

/**
 * DTO for avatar update response.
 */
@Value
public class UpdateAvatarResponseDto {
    /**
     * The updated avatar in Base64 format.
     */
    String avatar;

    /**
     * The timestamp when the avatar was updated.
     */
    LocalDateTime avatarUpdatedAt;
} 