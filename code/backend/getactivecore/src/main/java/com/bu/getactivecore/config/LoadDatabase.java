package com.bu.getactivecore.config;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;


@Slf4j
@Configuration
public class LoadDatabase {
    @Bean
    CommandLineRunner demoPreloadData(ActivityRepository activityRepo, UserRepository userRepo) {
        return args -> {
            Activity act1 = Activity.builder()
                    .name("Rock Climbing")
                    .startDateTime(LocalDateTime.now())
                    .location("Location")
                    .endDateTime(LocalDateTime.now())
                    .build();
            Activity act2 = Activity.builder()
                    .name("Yoga")
                    .location("Location")
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now())
                    .build();
            Activity act3 = Activity.builder()
                    .name("Running")
                    .location("Location")
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now())
                    .build();

            Users user1 = Users.builder()
                    .email("arsh@")
                    .username("arsh")
                    .password("arsh")
                    .accountState(AccountState.UNVERIFIED)
                    .build();
            Users user2 = Users.builder()
                    .email("arsh2@")
                    .username("arsh2")
                    .password("arsh2")
                    .accountState(AccountState.VERIFIED)
                    .build();

            log.info("Preloading {}", activityRepo.save(act1));
            log.info("Preloading {}", activityRepo.save(act2));
            log.info("Preloading {}", activityRepo.save(act3));
            log.info("Preloading {}", userRepo.save(user1));
            log.info("Preloading {}", userRepo.save(user2));
        };
    }
}
