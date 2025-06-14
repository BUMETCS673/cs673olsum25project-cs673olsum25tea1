package com.bu.getactivecore.service.users;

import static com.bu.getactivecore.shared.ErrorCode.DATA_STRUCTURE_INVALID;
import static com.bu.getactivecore.shared.ErrorCode.UNSUPPORTED_OPERATION;
import static com.bu.getactivecore.util.RestEndpoint.LOGIN;
import static com.bu.getactivecore.util.RestEndpoint.REGISTER;
import static com.bu.getactivecore.util.RestUtil.confirmRegistration;
import static com.bu.getactivecore.util.RestUtil.getToken;
import static com.bu.getactivecore.util.RestUtil.login;
import static com.bu.getactivecore.util.RestUtil.register;
import static com.bu.getactivecore.util.RestUtil.sendGet;
import static com.bu.getactivecore.util.RestUtil.sendPost;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.bu.getactivecore.config.JavaGmailMailConfig;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.email.EmailVerificationService;
import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.shared.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserLoginTest {

	private static final String VALID_PASSWORD = "Test123.";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private EmailVerificationService emailVerificationService;

	@MockitoBean
	private JavaGmailMailConfig javaGmailMailConfig;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void cleanup() {
		userRepository.deleteAll();
	}

	@Test
	void given_incorrect_http_request_type_then_4xxx_returned() throws Exception {
		sendGet(mockMvc, LOGIN, new LoginRequestDto("testuser", "testpassword")).andExpect(status().is4xxClientError())
				.andDo(print()).andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors.errorCode").value(UNSUPPORTED_OPERATION.getCode()));
	}

	@Test
	void given_invalid_param_keys_then_4xxx_returned() throws Exception {
		Map<String, String> loginReqData = Map.of("wrong_username_key", "testuser", "wrong_password_key",
				"testpassword");
		login(mockMvc, loginReqData).andExpect(status().is4xxClientError()).andDo(print())
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors.errorCode").value(DATA_STRUCTURE_INVALID.getCode()))
				.andExpect(jsonPath("$.errors.validationErrors").exists())
				.andExpect(jsonPath("$.errors.validationErrors.username").exists())
				.andExpect(jsonPath("$.errors.validationErrors.username").isArray())
				.andExpect(jsonPath("$.errors.validationErrors.username").isNotEmpty())
				.andExpect(jsonPath("$.errors.validationErrors.password").exists())
				.andExpect(jsonPath("$.errors.validationErrors.password").isArray())
				.andExpect(jsonPath("$.errors.validationErrors.password").isNotEmpty());
	}

	@Test
	void given_invalid_param_values_then_4xxx_returned() throws Exception {
		login(mockMvc, new LoginRequestDto("", "")).andExpect(status().is4xxClientError()).andDo(print())
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors.errorCode").value(DATA_STRUCTURE_INVALID.getCode()))
				.andExpect(jsonPath("$.errors.validationErrors").exists())
				.andExpect(jsonPath("$.errors.validationErrors.username").exists())
				.andExpect(jsonPath("$.errors.validationErrors.username").isArray())
				.andExpect(jsonPath("$.errors.validationErrors.username").isNotEmpty())
				.andExpect(jsonPath("$.errors.validationErrors.password").exists())
				.andExpect(jsonPath("$.errors.validationErrors.password").isArray())
				.andExpect(jsonPath("$.errors.validationErrors.password").isNotEmpty());
	}

	@Test
	void given_unregistered_user_and_login_is_attempted_then_4xxx_returned() throws Exception {
		login(mockMvc, new LoginRequestDto("testuser", "testpassword")).andExpect(status().is4xxClientError())
				.andDo(print());
	}

	@Test
	void verify_login_is_not_idempotent() throws Exception {
		RegistrationRequestDto registerReqDto = new RegistrationRequestDto("1234@bu.edu", "testuser", VALID_PASSWORD);

		String token = getToken(mockMvc, registerReqDto);
		assertNotNull(token, "Token should not be null after registration and login");

		// Artificial delay is needed to ensure the 'issued at' timestamp in the JWT is
		// different since that field is in seconds
		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		MvcResult response = login(mockMvc,
				new LoginRequestDto(registerReqDto.getUsername(), registerReqDto.getPassword()))
				.andExpect(status().is2xxSuccessful()).andDo(print()).andExpect(jsonPath("$.errors").doesNotExist())
				.andExpect(jsonPath("$.data").exists()).andExpect(jsonPath("$.data.token").exists())
				.andExpect(jsonPath("$.data.token").isNotEmpty()).andReturn();
		String token2 = objectMapper.readTree(response.getResponse().getContentAsString()).at("/data/token").asText();
		assertNotEquals(token, token2, "Login should not be idempotent, tokens should be different");
	}

	@Test
	void given_registered_user_and_wrong_username_used_then_4xxx_returned() throws Exception {
		String email = "1234@bu.edu";
		String username = "testuser";
		String password = VALID_PASSWORD;

		RegistrationRequestDto registerReqData = new RegistrationRequestDto(email, username, password);
		sendPost(mockMvc, REGISTER, registerReqData).andExpect(status().is2xxSuccessful());

		LoginRequestDto loginReqData = new LoginRequestDto("this_username_does_not_exist", password);
		sendPost(mockMvc, LOGIN, loginReqData).andExpect(status().is4xxClientError());
	}

	@Test
	void given_registered_user_and_wrong_password_used_then_4xxx_returned() throws Exception {
		String email = "1234@bu.edu";
		String username = "testuser";

		RegistrationRequestDto registerReqData = new RegistrationRequestDto(email, username, VALID_PASSWORD);
		sendPost(mockMvc, REGISTER, registerReqData).andExpect(status().is2xxSuccessful());

		sendPost(mockMvc, LOGIN, new LoginRequestDto(username, "this_password_does_not_match"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void given_unverified_registered_user_and_login_is_attempted_then_403_returned() throws Exception {
		String email = "1234@bu.edu";
		String username = "testuser";
		String password = VALID_PASSWORD;
		sendPost(mockMvc, REGISTER, new RegistrationRequestDto(email, username, password))
				.andExpect(status().is2xxSuccessful());

		sendPost(mockMvc, LOGIN, new LoginRequestDto(username, password)).andDo(print())
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors.errorCode").value(ErrorCode.VERIFIED_ACCOUNT_REQUIRED.getCode()));
	}

	@Test
	void verify_user_can_register_and_confirm_and_login() throws Exception {
		String email = "1234@bu.edu";
		String username = "testuser";
		String password = VALID_PASSWORD;
		RegistrationRequestDto req = new RegistrationRequestDto(email, username, password);
		MvcResult response = register(mockMvc, req).andExpect(status().is2xxSuccessful()).andReturn();
		String registrationToken = objectMapper.readTree(response.getResponse().getContentAsString()).at("/data/token")
				.asText();

		ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(registrationToken);
		confirmRegistration(mockMvc, confirmReq);

		sendPost(mockMvc, LOGIN, new LoginRequestDto(username, password)).andExpect(status().is2xxSuccessful())
				.andDo(print()).andExpect(jsonPath("$.errors").doesNotExist()).andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data.token").isNotEmpty());
	}

}