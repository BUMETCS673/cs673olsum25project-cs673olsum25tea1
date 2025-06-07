package com.bu.getactivecore.service.activity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDeleteRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityUpdateRequestDto;
import com.bu.getactivecore.shared.exception.ApiException;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @InjectMocks
    private ActivityService activityService;

    private String userId = "1";

    private String activityId = "1";

    @Test
    public void deleteActivityWithInValidActivityId() {
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build()));

        verify(userActivityRepository, never()).deleteByActivityId(activityId);

        verify(activityRepository, never()).deleteById(activityId);
    }

    @Test
    public void deleteActivityWithForceSetToTrue() {
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

        ActivityDeleteRequestDto requestDTO = ActivityDeleteRequestDto.builder().build();
        activityService.deleteActivity(activityId, requestDTO);

        verify(userActivityRepository).deleteByActivityId(activityId);

        verify(activityRepository).deleteById(activityId);
    }

    @Test
    public void deleteActivityWithForceSetToFalseAndHasParticipantsInActivity() {
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

        when(userActivityRepository.findByActivityIdAndRole(activityId, RoleType.PARTICIPANT))
                .thenReturn(List.of(new UserActivity()));

        assertThrows(ApiException.class,
                () -> activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build()));

        verify(userActivityRepository, never()).deleteByActivityId(activityId);

        verify(activityRepository, never()).deleteById(activityId);
    }

    @Test
    public void deleteActivitySuccessfully() {
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

        when(userActivityRepository.findByActivityIdAndRole(activityId, RoleType.PARTICIPANT))
                .thenReturn(new ArrayList<>());

        activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build());

        verify(userActivityRepository).deleteByActivityId(activityId);

        verify(activityRepository).deleteById(activityId);
    }

    @Test
    public void testGetActivities() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Activity> page = new PageImpl<>(List.of(Activity.builder().build()), pageable, 1);
        when(activityRepository.findAll(pageable)).thenReturn(page);

        activityService.getAllActivities(pageable);

        verify(activityRepository).findAll(pageable);
    }

    @Test
    public void testGetActivitiesByName() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Activity> page = new PageImpl<>(List.of(Activity.builder().build()), pageable, 1);
        when(activityRepository.findByNameContaining("Rock Climbing", pageable)).thenReturn(page);

        activityService.getActivityByName("Rock Climbing", pageable);

        verify(activityRepository).findByNameContaining("Rock Climbing", pageable);
    }

    @Test
    public void createActivitySuccessfully() {
        when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

        ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().plusHours(1))
                .endDateTime(LocalDateTime.now().plusHours(2))
                .build();

        Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
        createdActivity.setId(activityId);

        when(activityRepository.save(ActivityCreateRequestDto.from(dtoRequest))).thenReturn(createdActivity);

        UserActivity userActivityRole = UserActivity.builder()
                .userId(userId)
                .activity(createdActivity)
                .role(RoleType.ADMIN)
                .build();

        activityService.createActivity(userId, dtoRequest);

        verify(activityRepository).save(ActivityCreateRequestDto.from(dtoRequest));

        verify(userActivityRepository).save(userActivityRole);
    }

    @Test
    public void createActivityWithPastStartTime() {
        when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

        ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now().plusHours(2))
                .build();

        Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
        createdActivity.setId(activityId);

        UserActivity userActivityRole = UserActivity.builder()
                .userId(userId)
                .activity(createdActivity)
                .role(RoleType.ADMIN)
                .build();

        assertThrows(ApiException.class, () -> activityService.createActivity(userId, dtoRequest));

        verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

        verify(userActivityRepository, never()).save(userActivityRole);
    }

    @Test
    public void createActivityWithPastEndTime() {
        when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

        ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().minusHours(2))
                .build();

        Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
        createdActivity.setId(activityId);

        UserActivity userActivityRole = UserActivity.builder()
                .userId(userId)
                .activity(createdActivity)
                .role(RoleType.ADMIN)
                .build();

        assertThrows(ApiException.class, () -> activityService.createActivity(userId, dtoRequest));

        verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

        verify(userActivityRepository, never()).save(userActivityRole);
    }

    @Test
    public void createActivityWithEndTimeBeforeStartTime() {
        when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

        ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().plusHours(5))
                .endDateTime(LocalDateTime.now().plusHours(4))
                .build();

        Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
        createdActivity.setId(activityId);

        UserActivity userActivityRole = UserActivity.builder()
                .userId(userId)
                .activity(createdActivity)
                .role(RoleType.ADMIN)
                .build();

        assertThrows(ApiException.class, () -> activityService.createActivity(userId, dtoRequest));

        verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

        verify(userActivityRepository, never()).save(userActivityRole);
    }

    @Test
    public void updateActivityWithPastStartTime() {
        ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now().plusHours(2))
                .build();

        Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

        assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

        verify(activityRepository, never()).save(updateActivity);
    }

    @Test
    public void updateActivityWithPastEndTime() {
        ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().minusDays(2))
                .build();

        Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

        assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

        verify(activityRepository, never()).save(updateActivity);
    }

    @Test
    public void updateActivityWithEndTimeBeforeStartTime() {
        ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().plusHours(5))
                .endDateTime(LocalDateTime.now().plusHours(4))
                .build();

        Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

        assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

        verify(activityRepository, never()).save(updateActivity);
    }

    @Test
    public void updateActivitySuccessfully() {
        ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder()
                .name("Rock Climbing")
                .description("")
                .location("location")
                .startDateTime(LocalDateTime.now().plusHours(1))
                .endDateTime(LocalDateTime.now().plusHours(2))
                .build();

        Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

        when(activityRepository.save(updateActivity)).thenReturn(updateActivity);

        activityService.updateActivity(activityId, dtoRequest);

        verify(activityRepository).save(updateActivity);
    }

}
