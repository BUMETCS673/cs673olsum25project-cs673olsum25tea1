package com.bu.getactivecore.config;

import static com.bu.getactivecore.shared.Constants.PASSWORD_ENCODER_STRENGTH;
import static java.time.LocalDateTime.now;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.activity.UserActivity;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class LoadDatabase {
	@Bean
	CommandLineRunner demoPreloadData(ActivityRepository activityRepo, UserRepository userRepo,
			UserActivityRepository userActivityRepo) {
		return args -> {
			Activity act1 = Activity.builder() //
					.name("Rock Climbing") //
					.startDateTime(now().plusHours(1)) //
					.location("Campus Red Swan") //
					.endDateTime(now().plusHours(2)).build();
			Activity act2 = Activity.builder() //
					.name("Yoga") //
					.location("Gym") //
					.startDateTime(now()) //
					.endDateTime(now()) //
					.build();
			Activity act3 = Activity.builder() //
					.name("Running") //
					.location("Indoor track") //
					.startDateTime(now()) //
					.endDateTime(now()) //
					.build();

			Users user1 = Users.builder() //
					.email("arsh@edu.bu") //
					.username("arsh") //
					.password(new BCryptPasswordEncoder(PASSWORD_ENCODER_STRENGTH).encode("arsh")) //
					.accountState(AccountState.UNVERIFIED) //
					.build();
			Users user2 = Users.builder() //
					.email("arsh2@bu.edu") //
					.username("arsh2") //
					.password(new BCryptPasswordEncoder(PASSWORD_ENCODER_STRENGTH).encode("arsh")) //
					.accountState(AccountState.VERIFIED) //
					.build();
			log.info("Preloading {}", activityRepo.save(act1));
			log.info("Preloading {}", activityRepo.save(act2));
			log.info("Preloading {}", activityRepo.save(act3));
			log.info("Preloading {}", userRepo.save(user1));
			log.info("Preloading {}", userRepo.save(user2));

			userActivityRepo.save(UserActivity.builder().user(user2).activity(act1).role(RoleType.ADMIN).build());
			userActivityRepo.save(UserActivity.builder().user(user1).activity(act1).role(RoleType.PARTICIPANT).build());
		};
	}
}
