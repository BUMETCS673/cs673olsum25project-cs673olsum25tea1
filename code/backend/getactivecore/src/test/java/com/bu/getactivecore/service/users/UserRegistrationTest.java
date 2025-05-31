package com.bu.getactivecore.service.users;

import com.bu.getactivecore.config.JavaGmailMailConfig;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.email.EmailVerificationService;
import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.shared.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.bu.getactivecore.service.jwt.api.JwtApi.TokenClaimType;
import static com.bu.getactivecore.util.RestEndpoint.REGISTER;
import static com.bu.getactivecore.util.RestUtil.confirmRegistration;
import static com.bu.getactivecore.util.RestUtil.register;
import static com.bu.getactivecore.util.RestUtil.sendPost;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTest {

    private static final String VALID_PASSWORD = "Test123.";
    @MockitoBean
    private EmailVerificationService emailVerificationService;
    @MockitoBean
    private JavaGmailMailConfig javaGmailMailConfig;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtApi jwtApi;

    @AfterEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void given_empty_request_body_then_4xx_returned() throws Exception {
        mockMvc.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists());

        String invalidRequestJson = """
                {
                    "unknownkey": ""
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void given_invalid_content_type_then_4xx_returned() throws Exception {
        mockMvc.perform(post("/v1/register"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void given_non_bu_email_then_4xx_returned() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "1234@gmail.com",
                    "username": "testuser",
                    "password": "testpassword"
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.email").exists());
    }

    @Test
    void given_invalid_email_then_4xx_returned() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "",
                    "username": "testuser",
                    "password": "testpassword"
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.email").exists());
    }

    @Test
    void given_empty_username_then_4xx_returned() throws Exception {
        RegistrationRequestDto req = new RegistrationRequestDto("123@bu.edu", "", VALID_PASSWORD);
        register(mockMvc, req)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").exists())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").doesNotExist());
    }

    @Test
    void given_too_long_username_then_4xx_returned() throws Exception {
        RegistrationRequestDto req = new RegistrationRequestDto("123@bu.edu",
                "this_is_really_really_long_username_that_exceeds_the_maximum_length", VALID_PASSWORD);
        register(mockMvc, req)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").exists())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").doesNotExist());
    }


    @Test
    void given_empty_password_then_4xx_returned() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "123@bu.edu",
                    "username": "testusername",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").exists());
    }

    @Test
    void given_password_not_met_then_4xx_returned() throws Exception {
        RegistrationRequestDto missingUpperCaseReq = new RegistrationRequestDto("123@bu.edu",
                "123@bu.edu", "test123.");
        register(mockMvc, missingUpperCaseReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").exists());

        RegistrationRequestDto missingNumberCaseReq = new RegistrationRequestDto("123@bu.edu",
                "123@bu.edu", "Testtest.");
        register(mockMvc, missingNumberCaseReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").exists());
    }

    @Test
    void given_too_long_password_then_4xx_returned() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "123@bu.edu",
                    "username": "testusername",
                    "password": "this_is_really_really_long_password_that_exceeds_the_maximum_length"
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.email").doesNotExist())
                .andExpect(jsonPath("$.errors.validationErrors.password").exists());
    }

    @Test
    void given_multiple_validation_errors_then_multiple_validation_errors_and_4xx_are_returned() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "",
                    "username": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.validationErrors").exists())
                .andExpect(jsonPath("$.errors.validationErrors.username").isArray())
                .andExpect(jsonPath("$.errors.validationErrors.email").isArray())
                .andExpect(jsonPath("$.errors.validationErrors.password").isArray());
    }


    @Test
    void given_registration_attempted_with_same_credentials_then_4xx_returned() throws Exception {

        RegistrationRequestDto req = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);
        register(mockMvc, req)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        register(mockMvc, req)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.EMAIL_USERNAME_TAKEN.getCode()));
    }

    @Test
    void given_registration_attempted_with_different_username_then_4xx_returned() throws Exception {
        String email = "1234@bu.edu";
        String username = "test";
        String password = VALID_PASSWORD;
        RegistrationRequestDto req = new RegistrationRequestDto(email, username, password);
        register(mockMvc, req)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        RegistrationRequestDto DiffUsernameReq = new RegistrationRequestDto(email, "test2", password);
        register(mockMvc, DiffUsernameReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.EMAIL_USERNAME_TAKEN.getCode()));
    }

    @Test
    void verify_new_registered_user_is_assigned_UNVERIFIED_state() throws Exception {
        String username = "test";
        String email = "1234@bu.edu";
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto(email, username, VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());
        Optional<Users> testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        Assertions.assertEquals(AccountState.UNVERIFIED.name(), testUser.get().getAccountState().name());
    }

    @Test
    void given_registered_user_and_same_username_used_for_registration_then_4xx_returned() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        RegistrationRequestDto userReq2 = new RegistrationRequestDto("anothermail@bu.edu", "test", VALID_PASSWORD);
        register(mockMvc, userReq2)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.EMAIL_USERNAME_TAKEN.getCode()));
    }

    @Test
    void given_empty_confirmation_token_then_4xx_returned() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto("");
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.DATA_STRUCTURE_INVALID.getCode()));
    }

    @Test
    void given_invalid_confirmation_token_then_4xx_returned() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto("invalid.token");
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.TOKEN_INVALID.getCode()));
    }

    @Test
    void given_unknown_confirmation_user_token_then_4xx_returned() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        String token = jwtApi.generateToken("not_test_user");
        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(token);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.TOKEN_INVALID.getCode()));

        Optional<Users> registeredUser = userRepository.findByUsername("test");
        Assertions.assertTrue(registeredUser.isPresent());
        Assertions.assertEquals(AccountState.UNVERIFIED.name(), registeredUser.get().getAccountState().name());
    }


    @Test
    void given_non_confirmation_token_then_user_remains_UNVERIFIED() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        String token = jwtApi.generateToken("test");
        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(token);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.TOKEN_INVALID.getCode()));

        Optional<Users> registeredUser = userRepository.findByUsername("test");
        Assertions.assertTrue(registeredUser.isPresent());
        Assertions.assertEquals(AccountState.UNVERIFIED.name(), registeredUser.get().getAccountState().name());
    }

    @Test
    void given_confirmation_token_then_user_is_VERIFIED() throws Exception {
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto("1234@bu.edu", "test", VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        String token = jwtApi.generateToken("test", TokenClaimType.REGISTRATION_CONFIRMATION);
        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(token);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        Optional<Users> registeredUser = userRepository.findByUsername("test");
        Assertions.assertTrue(registeredUser.isPresent());
        Assertions.assertEquals(AccountState.VERIFIED.name(), registeredUser.get().getAccountState().name());
    }


    @Test
    void verify_register_confirmation_is_idempotent() throws Exception {
        String username = "test";
        String email = "1234@bu.edu";
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto(email, username, VALID_PASSWORD);

        register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist());

        String token = jwtApi.generateToken(username, TokenClaimType.REGISTRATION_CONFIRMATION);
        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(token);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        Optional<Users> testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        Assertions.assertEquals(AccountState.VERIFIED.name(), testUser.get().getAccountState().name());


        // Confirming again should not change the state
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        Assertions.assertEquals(AccountState.VERIFIED.name(), testUser.get().getAccountState().name());
    }


    @Test
    void given_invalid_confirmation_token_4xx_returned() throws Exception {
        String email = "1234@bu.edu";
        String username = "testuser";
        sendPost(mockMvc, REGISTER, new RegistrationRequestDto(email, username, VALID_PASSWORD)).andExpect(status().is2xxSuccessful());

        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto("invalid_token");
        confirmRegistration(mockMvc, confirmReq)
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.TOKEN_INVALID.getCode()));
    }
}