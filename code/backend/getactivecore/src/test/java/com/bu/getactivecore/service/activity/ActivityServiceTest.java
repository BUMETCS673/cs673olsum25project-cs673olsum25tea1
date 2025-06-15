package com.bu.getactivecore.service.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.activity.ActivityComment;
import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.ActivityCommentRepository;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.activity.entity.ActivityCommentCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDeleteRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityUpdateRequestDto;
import com.bu.getactivecore.shared.exception.ApiException;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

	private final String activityId = "1";

	@Mock
	private ActivityRepository activityRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserActivityRepository userActivityRepository;

	@Mock
	private ActivityCommentRepository activityCommentRepository;

	@InjectMocks
	private ActivityService activityService;

	private Users user;

	@BeforeEach
	void setUp() {
		user = Users.builder().userId("1").accountState(AccountState.VERIFIED).email("b.bu.edu").password("password")
				.build();
	}

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

	@Test
	void deleteActivityWithInValidActivityId() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

		assertThrows(ApiException.class,
				() -> activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build()));

		verify(userActivityRepository, never()).deleteByActivityId(activityId);

		verify(activityRepository, never()).deleteById(activityId);
	}

	@Test
	void deleteActivityWithForceSetToTrue() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityDeleteRequestDto requestDTO = ActivityDeleteRequestDto.builder().build();
		activityService.deleteActivity(activityId, requestDTO);

		verify(userActivityRepository).deleteByActivityId(activityId);

		verify(activityRepository).deleteById(activityId);
	}

	@Test
	void deleteActivityWithForceSetToFalseAndHasParticipantsInActivity() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		when(userActivityRepository.findByActivityIdAndRole(activityId, RoleType.PARTICIPANT))
				.thenReturn(List.of(new UserActivity()));

		assertThrows(ApiException.class,
				() -> activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build()));

		verify(userActivityRepository, never()).deleteByActivityId(activityId);

		verify(activityRepository, never()).deleteById(activityId);
	}

	@Test
	void deleteActivitySuccessfully() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		when(userActivityRepository.findByActivityIdAndRole(activityId, RoleType.PARTICIPANT))
				.thenReturn(new ArrayList<>());

		activityService.deleteActivity(activityId, ActivityDeleteRequestDto.builder().build());

		verify(userActivityRepository).deleteByActivityId(activityId);

		verify(activityRepository).deleteById(activityId);
	}

	@Test
	void testGetActivities() {
		PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
		Page<Activity> page = new PageImpl<>(List.of(Activity.builder().build()), pageable, 1);
		when(activityRepository.findAll(pageable)).thenReturn(page);

		activityService.getAllActivities(pageable);

		verify(activityRepository).findAll(pageable);
	}

	@Test
	void testGetActivitiesByName() {
		PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
		Page<Activity> page = new PageImpl<>(List.of(Activity.builder().build()), pageable, 1);
		when(activityRepository.findByNameContaining("Rock Climbing", pageable)).thenReturn(page);

		activityService.getActivityByName("Rock Climbing", pageable);

		verify(activityRepository).findByNameContaining("Rock Climbing", pageable);
	}

	@Test
	void testGetActivitiesSortedByPopularity() {
		PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
		Page<Activity> page = new PageImpl<>(List.of(Activity.builder().build()), pageable, 1);
		when(activityRepository.findAllSortedByPopularity(pageable)).thenReturn(page);

		activityService.getAllActivitiesSortedByPopularity(pageable);

		verify(activityRepository).findAllSortedByPopularity(pageable);
	}

	@Test
	void testGetAllActivityComments() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
		Page<ActivityComment> page = new PageImpl<>(List.of(ActivityComment.builder().build()), pageable, 1);
		when(activityCommentRepository.findAllByActivityId(pageable, activityId)).thenReturn(page);

		activityService.getAllActivityComments(pageable, activityId);

		verify(activityCommentRepository).findAllByActivityId(pageable, activityId);
	}

	@Test
	void createActivitySuccessfully() {
		when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

		ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().plusHours(1))
				.endDateTime(LocalDateTime.now().plusHours(2)).build();

		Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
		createdActivity.setId(activityId);

		when(activityRepository.save(ActivityCreateRequestDto.from(dtoRequest))).thenReturn(createdActivity);
		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

		UserActivity userActivityRole = UserActivity.builder().user(user).activity(createdActivity).role(RoleType.ADMIN)
				.build();

		activityService.createActivity(user.getUserId(), dtoRequest);

		verify(activityRepository).save(ActivityCreateRequestDto.from(dtoRequest));

		verify(userActivityRepository).save(userActivityRole);
	}

	@Test
	void createActivityWithPastStartTime() {
		when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

		ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().minusHours(1))
				.endDateTime(LocalDateTime.now().plusHours(2)).build();

		Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
		createdActivity.setId(activityId);

		UserActivity userActivityRole = UserActivity.builder().user(user).activity(createdActivity).role(RoleType.ADMIN)
				.build();

		assertThrows(ApiException.class, () -> activityService.createActivity(user.getUserId(), dtoRequest));

		verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

		verify(userActivityRepository, never()).save(userActivityRole);
	}

	@Test
	void createActivityWithPastEndTime() {
		when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

		ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now()).endDateTime(LocalDateTime.now().minusHours(2))
				.build();

		Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
		createdActivity.setId(activityId);

		UserActivity userActivityRole = UserActivity.builder().user(user).activity(createdActivity).role(RoleType.ADMIN)
				.build();

		assertThrows(ApiException.class, () -> activityService.createActivity(user.getUserId(), dtoRequest));

		verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

		verify(userActivityRepository, never()).save(userActivityRole);
	}

	@Test
	void createActivityWithEndTimeBeforeStartTime() {
		when(activityRepository.findByName("Rock Climbing")).thenReturn(Optional.empty());

		ActivityCreateRequestDto dtoRequest = ActivityCreateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().plusHours(5))
				.endDateTime(LocalDateTime.now().plusHours(4)).build();

		Activity createdActivity = ActivityCreateRequestDto.from(dtoRequest);
		createdActivity.setId(activityId);

		UserActivity userActivityRole = UserActivity.builder().user(user).activity(createdActivity).role(RoleType.ADMIN)
				.build();

		assertThrows(ApiException.class, () -> activityService.createActivity(user.getUserId(), dtoRequest));

		verify(activityRepository, never()).save(ActivityCreateRequestDto.from(dtoRequest));

		verify(userActivityRepository, never()).save(userActivityRole);
	}

	@Test
	void updateActivityWithPastStartTime() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().minusHours(1))
				.endDateTime(LocalDateTime.now().plusHours(2)).build();

		Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

		assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

		verify(activityRepository, never()).save(updateActivity);
	}

	@Test
	void updateActivityWithPastEndTime() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now()).endDateTime(LocalDateTime.now().minusDays(2))
				.build();

		Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

		assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

		verify(activityRepository, never()).save(updateActivity);
	}

	@Test
	void updateActivityWithEndTimeBeforeStartTime() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().plusHours(5))
				.endDateTime(LocalDateTime.now().plusHours(4)).build();

		Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

		assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

		verify(activityRepository, never()).save(updateActivity);
	}

	@Test
	void updateActivitySuccessfully() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().plusHours(1))
				.endDateTime(LocalDateTime.now().plusHours(2)).build();

		Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

		when(activityRepository.save(updateActivity)).thenReturn(updateActivity);

		activityService.updateActivity(activityId, dtoRequest);

		verify(activityRepository).save(updateActivity);
	}

	@Test
	void updateActivityWithActivityNotFound() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

		ActivityUpdateRequestDto dtoRequest = ActivityUpdateRequestDto.builder().name("Rock Climbing").description("")
				.location("location").startDateTime(LocalDateTime.now().plusHours(1))
				.endDateTime(LocalDateTime.now().plusHours(2)).build();

		Activity updateActivity = ActivityUpdateRequestDto.from(activityId, dtoRequest);

		assertThrows(ApiException.class, () -> activityService.updateActivity(activityId, dtoRequest));

		verify(activityRepository, never()).save(updateActivity);
	}

	@Test
	void joinActivitySuccessfully() {
		Activity activity = Activity.builder().id(activityId).build();
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.empty());
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

		activityService.joinActivity(user.getUserId(), activityId);

		verify(userActivityRepository).findByUserIdAndActivityId(user.getUserId(), activityId);
		verify(activityRepository).findById(activityId);
		verify(userActivityRepository)
				.save(UserActivity.builder().user(user).activity(activity).role(RoleType.PARTICIPANT).build());
	}

	@Test
	void joinActivityAlreadyJoined() {
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.of(new UserActivity()));

		assertThrows(ApiException.class, () -> activityService.joinActivity(user.getUserId(), activityId));
		verify(userActivityRepository).findByUserIdAndActivityId(user.getUserId(), activityId);
		verify(activityRepository, never()).findById(activityId);
		verify(userActivityRepository, never()).save(any());
	}

	@Test
	void joinActivityActivityNotFound() {
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.empty());
		when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

		assertThrows(ApiException.class, () -> activityService.joinActivity(user.getUserId(), activityId));
		verify(userActivityRepository).findByUserIdAndActivityId(user.getUserId(), activityId);
		verify(activityRepository).findById(activityId);
		verify(userActivityRepository, never()).save(any());
	}

	@Test
	void leaveActivitySuccessfully() {
		UserActivity userActivity = UserActivity.builder().user(user).build();
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.of(userActivity));

		activityService.leaveActivity(user.getUserId(), activityId);

		verify(userActivityRepository).findByUserIdAndActivityId(user.getUserId(), activityId);
		verify(userActivityRepository).delete(userActivity);
	}

	@Test
	void leaveActivityNotJoined() {
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.empty());

		activityService.leaveActivity(user.getUserId(), activityId);

		verify(userActivityRepository).findByUserIdAndActivityId(user.getUserId(), activityId);
		verify(userActivityRepository, never()).delete(any());
	}

	@Test
	void getJoinedActivitiesSuccessfully() {
		UserActivity userActivity = UserActivity.builder().user(user)
				.activity(Activity.builder().id(activityId).name("Test Activity").build()).role(RoleType.PARTICIPANT)
				.build();
		List<UserActivity> userActivities = List.of(userActivity);

		when(userActivityRepository.findJoinedActivitiesByUserId(user.getUserId())).thenReturn(userActivities);

		activityService.getJoinedActivities(user.getUserId());
		verify(userActivityRepository).findJoinedActivitiesByUserId(user.getUserId());
	}

	@Test
	void given_out_of_range_page_size_then_default_page_size_is_used() {
		UserActivity userActivity = UserActivity.builder().user(user)
				.activity(Activity.builder().id(activityId).name("Test Activity").build()).role(RoleType.PARTICIPANT)
				.build();

		when(activityRepository.findById(activityId))
				.thenReturn(Optional.of(Activity.builder().id(activityId).name("Test Activity").build()));
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.of(userActivity));

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		when(userActivityRepository.findParticipantsByActivityId(any(), any())).thenReturn(
				new PageImpl<>(List.of(userActivity), PageRequest.of(0, 20, Sort.by("name").ascending()), 1));

		activityService.getActivityRoster(user.getUserId(), activityId,
				PageRequest.of(0, 100, Sort.by("name").ascending()));
		verify(userActivityRepository).findParticipantsByActivityId(any(), pageableCaptor.capture());

		Pageable actual = pageableCaptor.getValue();
		Pageable expected = PageRequest.of(0, 20, Sort.by("user.username").ascending());
		assertEquals(expected, actual);
	}

	@Test
	void should_throw_IllegalArgumentException_for_invalid_page() {
		assertThrows(IllegalArgumentException.class, () -> {
			activityService.getActivityRoster(user.getUserId(), activityId,
					PageRequest.of(-5, 10, Sort.by("name").ascending())); // invalid page
		});
	}

	@Test
	void given_unknown_sort_field_then_default_sort_is_used() {
		UserActivity userActivity = UserActivity.builder().user(user)
				.activity(Activity.builder().id(activityId).name("Test Activity").build()).role(RoleType.PARTICIPANT)
				.build();
		when(activityRepository.findById(activityId))
				.thenReturn(Optional.of(Activity.builder().id(activityId).name("Test Activity").build()));
		when(userActivityRepository.findByUserIdAndActivityId(user.getUserId(), activityId))
				.thenReturn(Optional.of(userActivity));
		when(userActivityRepository.findParticipantsByActivityId(any(), any()))
				.thenReturn(new PageImpl<>(List.of(userActivity)));

		Pageable inputPageable = PageRequest.of(0, 10, Sort.by("unknownField").descending());
		activityService.getActivityRoster(user.getUserId(), activityId, inputPageable);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userActivityRepository).findParticipantsByActivityId(any(), pageableCaptor.capture());

		Pageable actual = pageableCaptor.getValue();
		Pageable expected = PageRequest.of(0, 10, ActivityService.DEFAULT_SORT);
		assertEquals(expected, actual);
	}

	@Test
	void createActivityCommentSuccessfully() {
		when(activityRepository.findById(activityId)).thenReturn(Optional.of(new Activity()));

		ActivityCommentCreateRequestDto dtoRequest = ActivityCommentCreateRequestDto.builder().comment("comment")
				.build();

		LocalDateTime timestamp = LocalDateTime.now();

		ActivityComment activityComment = ActivityComment.builder().activityId(activityId).userId(user.getUserId())
				.activityId(activityId).comment(dtoRequest.getComment()).timestamp(timestamp).build();

		activityService.createActivityComment(user.getUserId(), activityId, dtoRequest, timestamp);

		verify(activityCommentRepository).save(activityComment);
	}
}