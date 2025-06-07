package com.bu.getactivecore.service.activity;

import com.bu.getactivecore.config.JavaGmailMailConfig;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.activity.api.ActivityApi;
import com.bu.getactivecore.service.email.EmailVerificationService;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.shared.validation.AccountStateChecker;
import com.bu.getactivecore.util.RestEndpoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.bu.getactivecore.util.RestUtil.getToken;
import static com.bu.getactivecore.util.RestUtil.sendPost;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityTest {

    private static final String VALID_PASSWORD = "Test123.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AccountStateChecker accountChecker;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private JavaGmailMailConfig javaGmailMailConfig;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityApi activityApi;

    @Autowired
    private UserActivityRepository roleRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @AfterEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        activityRepository.deleteAll();
    }

    @Disabled
    @Test
    void given_user_who_activity_post_requested_without_admin_permission_then_403_returned() throws Exception {
        when(accountChecker.assertVerified(any(Authentication.class))).thenReturn(true);

        // Register & login user1 to get its token
        RegistrationRequestDto requestDto = new RegistrationRequestDto("user1@bu.edu", "user1", VALID_PASSWORD);
        String u1token = getToken(mockMvc, requestDto);

        // Then create an activity using user1
        Map<String, String> createActivityReq = Map.of(
                "name", "Test Activity",
                "location", "Test Location",
                "description", "Description",
                "startDateTime", "2011-12-03 10:15",
                "endDateTime", "2011-12-03 11:15"
        );

        MvcResult response = sendPost(mockMvc, RestEndpoint.ACTIVITY, createActivityReq, u1token)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
                
        // Then create user2 and get its token
       /* requestDto = new RegistrationRequestDto("user2@bu.edu", "user2", "testpassword");
        String u2token = getToken(mockMvc, requestDto);

        // When user2 is not admin of user1's activity, tries to update the activity, 403 should be returned.
        Map<String, String> updateActivityReq = Map.of(
                "activityId", activityId,
                "name", "Updated Activity Name"
        );
        sendPut(mockMvc, RestEndpoint.ACTIVITY, updateActivityReq, u2token)
                .andExpect(status().isForbidden())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.RESOURCE_ACCESS_DENIED.getCode()))
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").isNotEmpty())
                .andExpect(jsonPath("$.errors.validationErrors.permission").exists());*/
    }
}