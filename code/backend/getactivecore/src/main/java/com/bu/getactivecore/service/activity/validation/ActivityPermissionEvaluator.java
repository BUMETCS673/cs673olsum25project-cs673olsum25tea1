package com.bu.getactivecore.service.activity.validation;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ErrorCode;
import com.bu.getactivecore.shared.exception.ResourceAccessDeniedException;

/**
 * Contains methods to evaluate user permissions for activities. It is used in
 * Controllers to check if a user has admin permissions for a specific activity.
 */
@Component("activityPermissionEvaluator")
public class ActivityPermissionEvaluator {

	private final UserActivityRepository userActivityRepo;

	/**
	 * Constructs the permission evaluator with the provided
	 * {@link UserActivityRepository}.
	 *
	 * @param userActivityRepo used to fetch user roles for activities
	 */
	public ActivityPermissionEvaluator(UserActivityRepository userActivityRepo) {
		this.userActivityRepo = userActivityRepo;
	}

	/**
	 * Checks if the user has admin permissions for a specific activity.
	 *
	 * @param authentication the current user's authentication object
	 * @param activityId     the ID of the activity to check permissions for
	 * @return true if the user is an admin for the activity, otherwise exception is
	 *         thrown
	 * @throws ResourceAccessDeniedException if the user is not an admin of the
	 *                                       activity
	 */
	public boolean isAuthorizedToUpdateActivity(Authentication authentication, String activityId) {
		String userId = ((UserPrincipal) authentication.getPrincipal()).getUserDto().getUserId();
		return userActivityRepo.findByUserIdAndActivityId(userId, activityId)
				.map(role -> RoleType.ADMIN == role.getRole()).orElseThrow(() -> {
					String reason = String.format("User %s is not an admin of activity %s", userId, activityId);
					Map<String, List<String>> validationErrors = Map.of("permission",
							List.of("User is not the admin of given activity"));
					ApiErrorPayload error = ApiErrorPayload.builder().status(HttpStatus.FORBIDDEN)
							.errorCode(ErrorCode.RESOURCE_ACCESS_DENIED).message(reason)
							.validationErrors(validationErrors).build();
					return new ResourceAccessDeniedException(error);
				});
	}
}
