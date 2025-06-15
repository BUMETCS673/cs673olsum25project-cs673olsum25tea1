package com.bu.getactivecore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bu.getactivecore.model.activity.ActivityComment;

/**
 * Repository interface for ActivityComment entity.
 */
public interface ActivityCommentRepository extends JpaRepository<ActivityComment, String> {

	Page<ActivityComment> findAllByActivityId(Pageable pageable, String activityId);

}
