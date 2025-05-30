package com.bu.getactivecore.service.activity.api;

import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Interface for managing activities.
 */
public interface ActivityApi {

    /**
     * Retrieves all activities.
     *
     * @return List of all activities
     */
    List<ActivityDto> getAllActivities();

    /**
     * Retrieves activities by their name.
     *
     * @param activityName Name of the activity to search for
     * @return List of activities matching the given name
     */
    List<ActivityDto> getActivityByName(String activityName);

    /**
     * Creates a new activity.
     *
     * @param userId     ID of the user creating the activity
     * @param requestDto Details of the activity to be created
     * @return Response containing details of the created activity
     */
    void createActivity(String userId, @Valid ActivityCreateRequestDto requestDto);
}
