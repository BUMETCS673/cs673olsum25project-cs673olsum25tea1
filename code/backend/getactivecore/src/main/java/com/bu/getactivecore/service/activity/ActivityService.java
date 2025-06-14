package com.bu.getactivecore.service.activity;

import static com.bu.getactivecore.model.activity.RoleType.ADMIN;
import static com.bu.getactivecore.model.activity.RoleType.PARTICIPANT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.activity.api.ActivityApi;
import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDeleteRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import com.bu.getactivecore.service.activity.entity.ActivityUpdateRequestDto;
import com.bu.getactivecore.service.activity.entity.UserActivityDto;
import com.bu.getactivecore.service.users.entity.ParticipantDto;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

/**
 * Core logic for managing activities.
 */
@Slf4j
@Service
public class ActivityService implements ActivityApi {

	private final UserActivityRepository m_userActivityRepo;

	private final ActivityRepository m_activityRepo;

	private final UserRepository m_userRepo;

	/**
	 * Constructs the ActivityService.
	 *
	 * @param activityRepo     used to fetch and manage activities
	 * @param userActivityRepo used to fetch and manage user activities
	 */
	public ActivityService(ActivityRepository activityRepo, UserActivityRepository userActivityRepo,
			UserRepository userRepo) {
		m_activityRepo = activityRepo;
		m_userActivityRepo = userActivityRepo;
		m_userRepo = userRepo;
	}

	@Override
	public Page<ActivityDto> getActivityByName(String activityName, Pageable pageable) {
		Page<Activity> activities = m_activityRepo.findByNameContaining(activityName, pageable);
		return activities.map(ActivityDto::of);
	}

	@Override
	public Page<ActivityDto> getAllActivities(Pageable pageable) {
		Page<Activity> activities = m_activityRepo.findAll(pageable);
		return activities.map(ActivityDto::of);
	}

	@Override
	@Transactional
	public void createActivity(String userId, ActivityCreateRequestDto requestDto) {
		m_activityRepo.findByName(requestDto.getName()).ifPresent(a -> {
			throw new ApiException(
					ApiErrorPayload.builder().status(BAD_REQUEST).message("Activity name exists").build());
		});

		if (requestDto.getEndDateTime().isEqual(requestDto.getStartDateTime())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("End date time cannot be on or before start date time").build());
		}

		if (requestDto.getStartDateTime().isBefore(LocalDateTime.now())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("Start date time cannot be in the past").build());
		}

		if (requestDto.getEndDateTime().isBefore(LocalDateTime.now())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("End date time cannot be in the past").build());
		}

		Activity createdActivity = m_activityRepo.save(ActivityCreateRequestDto.from(requestDto));
		Users user = getUserById(userId);
		UserActivity userActivityRole = UserActivity.builder().user(user).activity(createdActivity).role(ADMIN).build();
		m_userActivityRepo.save(userActivityRole);
	}

	private Users getUserById(String userId) {
		return m_userRepo.findById(userId).orElseThrow(() -> new ApiException(
				ApiErrorPayload.builder().status(BAD_REQUEST).message("User not found").build()));
	}

	@Override
	public void deleteActivity(String activityId, ActivityDeleteRequestDto requestDto) {
		Optional<Activity> activity = m_activityRepo.findById(activityId);

		if (activity.isEmpty()) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST).message("Activity not found").build());
		}

		List<UserActivity> userActivities = m_userActivityRepo.findByActivityIdAndRole(activityId, PARTICIPANT);
		if (!requestDto.isForce() && !userActivities.isEmpty()) {
			throw new ApiException(ApiErrorPayload.builder().status(FORBIDDEN)
					.message("Force is set to false. There are participants in this activity. ").build());
		}

		m_userActivityRepo.deleteByActivityId(activityId);
		m_activityRepo.deleteById(activityId);
	}

	@Override
	public ActivityDto updateActivity(String id, ActivityUpdateRequestDto requestDto) {
		if (requestDto.getEndDateTime().isBefore(LocalDateTime.now())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("End date time cannot be in the past").build());
		}

		if (requestDto.getEndDateTime().isEqual(requestDto.getStartDateTime())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("End date time cannot be on or before start date time").build());
		}

		if (requestDto.getStartDateTime().isBefore(LocalDateTime.now())
				|| requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
			throw new ApiException(ApiErrorPayload.builder().status(BAD_REQUEST)
					.message("Start date time cannot be in the past").build());
		}

		Activity updateActivity = m_activityRepo.save(ActivityUpdateRequestDto.from(id, requestDto));
		return ActivityDto.of(updateActivity);
	}

	@Override
	public List<UserActivityDto> getJoinedActivities(String userId) {
		return m_userActivityRepo.findJoinedActivitiesByUserId(userId).stream().map(UserActivityDto::of).toList();
	}

	@Override
	public void joinActivity(String userId, String activityId) {
		m_userActivityRepo.findByUserIdAndActivityId(userId, activityId).ifPresent(userActivity -> {
			throw new ApiException(
					ApiErrorPayload.builder().status(BAD_REQUEST).message("User already joined activity").build());
		});
		Activity activity = m_activityRepo.findById(activityId).orElseThrow(() -> new ApiException(
				ApiErrorPayload.builder().status(BAD_REQUEST).message("Activity does not exist").build()));

		UserActivity userActivity = UserActivity.builder() //
				.user(getUserById(userId)) //
				.activity(activity) //
				.role(PARTICIPANT) //
				.build();
		m_userActivityRepo.save(userActivity);
	}

	@Override
	public void leaveActivity(String userId, String activityId) {
		m_userActivityRepo.findByUserIdAndActivityId(userId, activityId).ifPresentOrElse(m_userActivityRepo::delete,
				() -> log.warn("No activity found for user '{}' with activity ID '{}' to leave", userId, activityId));
	}

	@Override
	public Page<ParticipantDto> getActivityRoster(String requestedUserId, String activityId, Pageable pageable) {
		m_activityRepo.findById(activityId).orElseThrow(() -> new ApiException(ApiErrorPayload.builder() //
				.status(NOT_FOUND) //
				.message("Activity does not exist") //
				.debugMessage("Activity not found: '" + activityId + "'") //
				.build()));

		m_userActivityRepo.findByUserIdAndActivityId(requestedUserId, activityId).orElseThrow(() -> {
			String debugMessage = String.format("User '%s' does not have access to activity '%s'", requestedUserId,
					activityId);
			return new ApiException(ApiErrorPayload.builder() //
					.status(FORBIDDEN) //
					.message("User does not have access to this activity") //
					.debugMessage(debugMessage).build());
		});

		Page<UserActivity> result = m_userActivityRepo.findParticipantsByActivityId(activityId, pageable);
		return result.map(ParticipantDto::of);
	}
}
