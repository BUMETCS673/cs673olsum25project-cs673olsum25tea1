package com.bu.getactivecore.service.users;

import com.bu.getactivecore.config.JavaGmailMailConfig;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.email.api.EmailApi;
import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.ConfirmationResendRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static com.bu.getactivecore.service.jwt.api.JwtApi.TokenClaimType.REGISTRATION_CONFIRMATION;
import static com.bu.getactivecore.util.JwtTestUtil.extractClaim;
import static com.bu.getactivecore.util.RestUtil.confirmRegistration;
import static com.bu.getactivecore.util.RestUtil.register;
import static com.bu.getactivecore.util.RestUtil.resendConfirmRegistration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResendConfirmationTest {

    private static final String VALID_PASSWORD = "Test123.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailApi emailApi;

    @MockitoBean
    private JavaGmailMailConfig javaGmailMailConfig;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;


    @AfterEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void given_unknown_email_or_username_then_204_returned() throws Exception {
        String username = "test";
        String email = "1234@bu.edu";
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto(email, username, VALID_PASSWORD);

        MvcResult response = register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String registrationToken = objectMapper.readTree(response.getResponse().getContentAsString())
                .at("/data/token")
                .asText();
        Assertions.assertNotNull(registrationToken);


        // Request to resend confirmation
        ConfirmationResendRequestDto unknownEmail = new ConfirmationResendRequestDto("123.nonregistered_email@bu.edu", username);
        resendConfirmRegistration(mockMvc, unknownEmail)
                .andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());

        ConfirmationResendRequestDto unknownUsername = new ConfirmationResendRequestDto(email, "unknownUsername");
        resendConfirmRegistration(mockMvc, unknownUsername)
                .andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());


        // Confirm confirmation was not resent
        verify(emailApi, times(1)).sendVerificationEmail(emailCaptor.capture(), tokenCaptor.capture());
        assertEquals(email, emailCaptor.getValue());
        assertEquals(registrationToken, tokenCaptor.getValue());

        Optional<Users> testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        assertEquals(AccountState.UNVERIFIED.name(), testUser.get().getAccountState().name());
    }

    @Test
    void given_valid_email_and_username_then_confirmation_is_resent() throws Exception {
        String username = "test";
        String email = "1234@bu.edu";
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto(email, username, VALID_PASSWORD);

        MvcResult response = register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String registrationToken = objectMapper.readTree(response.getResponse().getContentAsString())
                .at("/data/token")
                .asText();
        Assertions.assertNotNull(registrationToken);
        Assertions.assertEquals(REGISTRATION_CONFIRMATION.name(), extractClaim(registrationToken, "type"));

        // Simulate a delay to ensure the token is different when resent
        try {
            CountDownLatch latch = new CountDownLatch(1);
            latch.await(2000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Request to resend confirmation
        ConfirmationResendRequestDto resendReq = new ConfirmationResendRequestDto(email, username);
        resendConfirmRegistration(mockMvc, resendReq)
                .andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());

        // Confirm confirmation was resent
        verify(emailApi, times(2)).sendVerificationEmail(emailCaptor.capture(), tokenCaptor.capture());
        assertEquals(email, emailCaptor.getAllValues().get(1));
        assertNotEquals(registrationToken, tokenCaptor.getAllValues().get(1));
        Assertions.assertEquals(REGISTRATION_CONFIRMATION.name(), extractClaim(tokenCaptor.getAllValues().get(1), "type"));

        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(registrationToken);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        Optional<Users> testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        assertEquals(AccountState.VERIFIED.name(), testUser.get().getAccountState().name());
    }


    @Test
    void given_verified_user_then_confirmation_is_not_resent_and_204_returned() throws Exception {
        String username = "test";
        String email = "1234@bu.edu";
        RegistrationRequestDto validUserReq1 = new RegistrationRequestDto(email, username, VALID_PASSWORD);

        MvcResult response = register(mockMvc, validUserReq1)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String registrationToken = objectMapper.readTree(response.getResponse().getContentAsString())
                .at("/data/token")
                .asText();
        ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(registrationToken);
        confirmRegistration(mockMvc, confirmReq)
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        Optional<Users> testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        assertEquals(AccountState.VERIFIED.name(), testUser.get().getAccountState().name());

        // User is now verified, so confirmation should not be resent
        ConfirmationResendRequestDto resendReq = new ConfirmationResendRequestDto(email, username);
        resendConfirmRegistration(mockMvc, resendReq)
                .andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());

        // Confirm confirmation was resent
        verify(emailApi, times(1)).sendVerificationEmail(emailCaptor.capture(), tokenCaptor.capture());
        assertEquals(email, emailCaptor.getValue());
        assertEquals(registrationToken, tokenCaptor.getValue());

        // Verify user state remains verified
        testUser = userRepository.findByUsername(username);
        Assertions.assertTrue(testUser.isPresent());
        assertEquals(AccountState.VERIFIED.name(), testUser.get().getAccountState().name());
    }
}