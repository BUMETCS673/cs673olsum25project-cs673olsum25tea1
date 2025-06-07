package com.bu.getactivecore.service.activity.api;

import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDeleteRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import com.bu.getactivecore.service.activity.entity.ActivityUpdateRequestDto;
import com.bu.getactivecore.service.activity.entity.UserActivityDto;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for managing activities.
 */
public interface ActivityApi {

    /**
     * Retrieves all activities.
     *
     * @return List of all activities
     */
    Page<ActivityDto> getAllActivities(Pageable page);

    /**
     * Retrieves activities by their name.
     *
     * @param activityName Name of the activity to search for
     * @return List of activities matching the given name
     */
    Page<ActivityDto> getActivityByName(String activityName, Pageable page);

    /**
     * Creates a new activity.
     *
     * @param userId     ID of the user creating the activity
     * @param requestDto Details of the activity to be created
     * @return Response containing details of the created activity
     */
    void createActivity(String userId, @Valid ActivityCreateRequestDto requestDto);

    /**
     * Delete an activity.
     *
     * @param activityId ID of a to be deleted activity
     * @param requestDto Details of the activity to be deleted
     */
    void deleteActivity(String activityId, @Valid ActivityDeleteRequestDto requestDto);

    /**
     * Update an activity.
     *
     * @param activityId ID of a to be deleted activity
     * @param requestDto Details of the activity to be updated
     * @return Response containing details of the updated activity
     */
    ActivityDto updateActivity(String activityId, @Valid ActivityUpdateRequestDto requestDto);

    /**
     * Retrieves activities where the user is a participant.
     *
     * @param userId ID of the user
     * @return List of activities where the user is a participant
     */
    List<UserActivityDto> getParticipantActivities(String userId);

    /**
     * Joins an activity.
     *
     * @param userId     ID of the user joining the activity
     * @param activityId ID of the activity to join
     */
    void joinActivity(String userId, String activityId);

    /**
     * Leaves an activity.
     *
     * @param userId     ID of the user leaving the activity
     * @param activityId ID of the activity to leave
     */
    void leaveActivity(String userId, String activityId);
}
