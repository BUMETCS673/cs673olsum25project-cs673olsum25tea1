package com.bu.getactivecore.service.activity.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDeleteRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import com.bu.getactivecore.service.activity.entity.ActivityUpdateRequestDto;
import com.bu.getactivecore.service.activity.entity.UserActivityDto;
import com.bu.getactivecore.service.users.entity.ParticipantDto;

import jakarta.validation.Valid;

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
     * Retrieves a list of joined activities for the requested user.
     *
     * @param userId ID of the user whose joined activities are to be fetched
     * @return List of {@link UserActivityDto} representing the activities the user has joined
     */
    List<UserActivityDto> getJoinedActivities(String userId);

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

	/**
	 * Gets the roster of participants for a specific activity.
	 *
	 * @param requestedUserId ID of the user requesting the participant list.
	 * @param activityId      ID of the activity for which participants are to be
	 *                        fetched.
	 * @param pageable        Pagination information.
	 * @return A paginated list of {@link ParticipantDto} objects.
	 */
	Page<ParticipantDto> getActivityRoster(String requestedUserId, String activityId, Pageable pageable);

}
