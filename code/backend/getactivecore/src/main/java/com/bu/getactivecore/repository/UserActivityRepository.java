package com.bu.getactivecore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;

/**
 * Repository interface for managing user activity roles.
 */
public interface UserActivityRepository extends JpaRepository<UserActivity, String> {

	/**
	 * Finds the {@link UserActivity} based on given parameters.
	 *
	 * @param userId     The ID of the user to search its activity role.
	 * @param activityId The ID of the activity to search the given user's role in.
	 * @return {@link UserActivity} if found, otherwise {@link Optional#empty()}.
	 */
	@Query("SELECT ua FROM UserActivity ua WHERE ua.user.userId = :userId AND ua.activity.id = :activityId")
	Optional<UserActivity> findByUserIdAndActivityId(String userId, String activityId);

	/**
	 * Finds the {@link List of UserActivity} based on given parameters.
	 *
	 * @param activityId The ID of the user to search its activity role.
	 * @param role       The user's role in an activity.
	 * @return {@link UserActivity} if found, otherwise {@link Optional#empty()}.
	 */
	List<UserActivity> findByActivityIdAndRole(String activityId, RoleType role);

	/**
	 * Delete an activity based on given parameters.
	 *
	 * @param activityId The ID of the user to search its activity role.
	 */
	void deleteByActivityId(String activityId);

//    @Query("SELECT ua FROM UserActivity ua JOIN FETCH ua.activity WHERE ua.userId = :userId")
//    List<UserActivity> findJoinedActivitiesByUserId(String userId);

	@Query("SELECT ua FROM UserActivity ua JOIN ua.activity JOIN ua.user WHERE ua.user.userId = :userId")
	List<UserActivity> findJoinedActivitiesByUserId(String userId);

	// @Query(value="SELECT ua FROM user_activities ua JOIN FETCH ua.activity a JOIN
	// users u ON ua.user.userId = u.userId " + "WHERE a.id = :activityId",
	// nativeQuery = true)
	@Query("SELECT ua FROM UserActivity ua JOIN ua.activity a JOIN ua.user u WHERE a.id = :activityId")
	Page<UserActivity> findParticipantsByActivityId(@Param("activityId") String activityId, Pageable pageable);
}