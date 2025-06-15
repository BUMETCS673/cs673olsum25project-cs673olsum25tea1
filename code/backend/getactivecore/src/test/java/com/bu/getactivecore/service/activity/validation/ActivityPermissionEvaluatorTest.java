package com.bu.getactivecore.service.activity.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.service.users.entity.UserDto;
import com.bu.getactivecore.shared.ErrorCode;
import com.bu.getactivecore.shared.exception.ResourceAccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ActivityPermissionEvaluatorTest {

	private static final String USER_ID = "7e5453ae-952d-4728-86b5-94c8dff85464";
	private static final String ACTIVITY_ID = "163cc2e7-0aca-49ff-afef-aaa948e8296e";
	@Mock
	private UserActivityRepository userActivityRepository;
	@Mock
	private Authentication authentication;
	@Mock
	private UserPrincipal userPrincipal;
	@InjectMocks
	private ActivityPermissionEvaluator evaluator;


	@BeforeEach
	void setup() {
		when(authentication.getPrincipal()).thenReturn(userPrincipal);
		UserDto userDto = UserDto.builder().userId(USER_ID).build();
		when(userPrincipal.getUserDto()).thenReturn(userDto);
	}

	@Test
	void given_admin_user_when_checking_activity_update_permission_then_return_true() {
		UserActivity userActivity = UserActivity.builder().user(Users.builder().userId(USER_ID).build())
				.activity(Activity.builder().id(ACTIVITY_ID).build()).role(RoleType.ADMIN).build();

		when(userActivityRepository.findByUserIdAndActivityId(USER_ID, ACTIVITY_ID))
				.thenReturn(Optional.of(userActivity));

		try {
			evaluator.assertAuthorizedToUpdateActivity(authentication, ACTIVITY_ID);
		} catch (ResourceAccessDeniedException e) {
			fail("Expected no exception for admin user", e);
		}
		verify(userActivityRepository).findByUserIdAndActivityId(USER_ID, ACTIVITY_ID);
	}

	@Test
	void given_participant_then_throw_exception() {
		UserActivity userActivity = UserActivity.builder().user(Users.builder().userId(USER_ID).build())
				.activity(Activity.builder().id(ACTIVITY_ID).build()).role(RoleType.PARTICIPANT) // not admin
				.build();

		when(userActivityRepository.findByUserIdAndActivityId(USER_ID, ACTIVITY_ID))
				.thenReturn(Optional.of(userActivity));

		ResourceAccessDeniedException ex = assertThrows(ResourceAccessDeniedException.class,
				() -> evaluator.assertAuthorizedToUpdateActivity(authentication, ACTIVITY_ID));

		assertEquals(HttpStatus.FORBIDDEN, ex.getError().getStatus());
		assertEquals(ErrorCode.RESOURCE_ACCESS_DENIED, ex.getError().getErrorCode());
	}

	@Test
	void given_no_activity_participation_then_throw_exception() {
		when(userActivityRepository.findByUserIdAndActivityId(USER_ID, ACTIVITY_ID)).thenReturn(Optional.empty());

		ResourceAccessDeniedException ex = assertThrows(ResourceAccessDeniedException.class,
				() -> evaluator.assertAuthorizedToUpdateActivity(authentication, ACTIVITY_ID));

		assertEquals(HttpStatus.FORBIDDEN, ex.getError().getStatus());
		assertEquals(ErrorCode.RESOURCE_ACCESS_DENIED, ex.getError().getErrorCode());
	}
}