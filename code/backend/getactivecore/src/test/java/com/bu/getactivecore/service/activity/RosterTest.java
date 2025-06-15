package com.bu.getactivecore.service.activity;

import static com.bu.getactivecore.util.RestEndpoint.LOGIN;
import static com.bu.getactivecore.util.RestEndpoint.PARTICIPANTS;
import static com.bu.getactivecore.util.RestUtil.confirmRegistration;
import static com.bu.getactivecore.util.RestUtil.register;
import static com.bu.getactivecore.util.RestUtil.sendGet;
import static com.bu.getactivecore.util.RestUtil.sendPost;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.bu.getactivecore.config.JavaGmailMailConfig;
import com.bu.getactivecore.model.activity.RoleType;
import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.ActivityRepository;
import com.bu.getactivecore.repository.UserActivityRepository;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.activity.api.ActivityApi;
import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import com.bu.getactivecore.service.email.EmailVerificationService;
import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.shared.validation.AccountStateChecker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class RosterTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final String EMAIL1 = "1234@bu.edu";

	private final String USERNAME1 = "testuser";

	private final String PASSWORD = "Test123.";

	@Autowired
	ActivityApi activityApi;

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
	private UserActivityRepository userActivityRepo;

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private JwtApi jwtApi;

	private String user1AccessToken;

	@AfterEach
	void cleanup() {
		userActivityRepo.deleteAll();
		userRepository.deleteAll();
		activityRepository.deleteAll();
	}

	@BeforeEach
	void setup() throws Exception {
		when(accountChecker.assertVerified(any(Authentication.class))).thenReturn(true);
		user1AccessToken = registerUser(EMAIL1, USERNAME1, PASSWORD);
	}

	private int extractPageNumber(String url) {
		try {
			UriComponents uriComponents = UriComponentsBuilder.fromUriString(url).build();
			return Integer.parseInt(Objects.requireNonNull(uriComponents.getQueryParams().getFirst("page")));
		} catch (Exception e) {
			fail("Invalid pagination URL: " + url);
			return -1;
		}
	}

	private void createActivity(String username, ActivityCreateRequestDto createActivityRequest) {
		Users user1 = userRepository.findByUsername(username).orElseThrow();
		activityApi.createActivity(user1.getUserId(), createActivityRequest);
	}

	private String registerUser(String email, String username, String password) throws Exception {
		RegistrationRequestDto req = new RegistrationRequestDto(email, username, password);
		MvcResult response = register(mockMvc, req).andExpect(status().is2xxSuccessful()).andReturn();
		String registrationToken = objectMapper.readTree(response.getResponse().getContentAsString()).at("/data/token")
				.asText();

		ConfirmationRequestDto confirmReq = new ConfirmationRequestDto(registrationToken);
		confirmRegistration(mockMvc, confirmReq);

		response = sendPost(mockMvc, LOGIN, new LoginRequestDto(username, password))
				.andExpect(status().is2xxSuccessful()).andDo(print()).andReturn();
		return objectMapper.readTree(response.getResponse().getContentAsString()).at("/data/token").asText();
	}

	@Test
	void given_activity_created_and_no_participants_then_admin_user_is_returned() throws Exception {
		ActivityCreateRequestDto createActReq = ActivityCreateRequestDto.builder().name("Title").description("Desc")
				.location("moon").startDateTime(now().plusHours(1)).endDateTime(now().plusHours(5)).build();
		createActivity(USERNAME1, createActReq);

		ActivityDto activityDto = activityApi.getActivityByName("Title", Pageable.unpaged()).get().findFirst().get();
		Map<String, String> pathParams = Map.of("activityId", activityDto.getId());
		sendGet(mockMvc, PARTICIPANTS, pathParams, user1AccessToken).andExpect(status().is2xxSuccessful())
				.andDo(print()).andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].username").value(USERNAME1))
				.andExpect(jsonPath("$.data.content[0].roleType").value(RoleType.ADMIN.name()));
	}

	@Test
	void given_non_existent_activity_then_4xx_is_returned() throws Exception {
		ActivityCreateRequestDto createActReq = ActivityCreateRequestDto.builder().name("Title").description("Desc")
				.location("moon").startDateTime(now().plusHours(1)).endDateTime(now().plusHours(5)).build();

		createActivity(USERNAME1, createActReq);
		Map<String, String> pathParams = Map.of("activityId", "wrong_id");
		sendGet(mockMvc, PARTICIPANTS, pathParams, user1AccessToken).andExpect(status().is4xxClientError());
	}

	@Test
	void should_return_403_when_user_requests_roster_without_participation() throws Exception {
		ActivityCreateRequestDto createActivity1 = ActivityCreateRequestDto.builder().name("Title").description("Desc")
				.location("moon").startDateTime(now().plusHours(1)).endDateTime(now().plusHours(5)).build();

		createActivity(USERNAME1, createActivity1);

		ActivityCreateRequestDto createActivity2 = ActivityCreateRequestDto.builder().name("Title2")
				.description("Desc2").location("moon2").startDateTime(now().plusHours(1))
				.endDateTime(now().plusHours(5)).build();

		String email2 = "2@bu.edu";
		String username2 = "testuser2";
		String password2 = "Test123.";
		String user2AccessToken = registerUser(email2, username2, password2);
		createActivity(username2, createActivity2);

		ActivityDto activity2 = activityApi.getActivityByName(createActivity2.getName(), Pageable.unpaged()).get()
				.findFirst().get();

		Map<String, String> pathParams = Map.of("activityId", activity2.getId());
		sendGet(mockMvc, PARTICIPANTS, pathParams, user1AccessToken).andExpect(status().isForbidden());

		sendGet(mockMvc, PARTICIPANTS, pathParams, user2AccessToken) //
				.andExpect(status().is2xxSuccessful()) //
				.andDo(print()).andExpect(jsonPath("$.data.content").isArray()) //
				.andExpect(jsonPath("$.data.content.length()").value(1)) //
				.andExpect(jsonPath("$.data.content[0].username").value(username2)) //
				.andExpect(jsonPath("$.data.content[0].roleType").value(RoleType.ADMIN.name()));
	}

	@Test
	void verify_roster_is_paginated() throws Exception {
		// 1. Create an activity
		ActivityCreateRequestDto createActivityRequest = ActivityCreateRequestDto.builder()
				.name("Pagination Test Activity").description("Testing pagination with many participants")
				.location("Earth").startDateTime(now().plusDays(1)).endDateTime(now().plusDays(2)).build();
		createActivity(USERNAME1, createActivityRequest);

		ActivityDto activity = activityApi.getActivityByName("Pagination Test Activity", Pageable.unpaged()).get()
				.findFirst().orElseThrow();
		// 2. Create users and join them to the activity
		for (int i = 0; i < 5; i++) {
			String username = "user" + i;
			String email = username + "@bu.edu";
			Users user = Users.builder().username(username).email(email).password(PASSWORD)
					.accountState(AccountState.VERIFIED).build();
			userRepository.save(user);
			activityApi.joinActivity(user.getUserId(), activity.getId());
		}

		// 3. Call GET /activities/{activityId}/participants?page=0&size=3
		Map<String, String> pathParams = Map.of("activityId", activity.getId());
		Map<String, String> pageParams = Map.of("page", "0", "size", "3");
		MvcResult response = sendGet(mockMvc, PARTICIPANTS, pathParams, user1AccessToken, pageParams).andDo(print())
				.andExpect(status().isOk()).andReturn();
		JsonNode responseData = objectMapper.readTree(response.getResponse().getContentAsString());

		JsonNode content = responseData.at("/data/content");
		assertNotNull(content);
		assertEquals(3, content.size(), "Expected 3 participants on page 0");

		JsonNode pageData = responseData.at("/data");
		assertNotNull(pageData);
		assertEquals(0, pageData.at("/number").asInt());
		assertEquals(3, pageData.at("/size").asInt());
		assertEquals(6, pageData.at("/totalElements").asInt());
		assertEquals(2, pageData.at("/totalPages").asInt());
	}

	@Test
	void verify_next_and_previous_page_urls_return_correct_data() throws Exception {
		// Create activity
		ActivityCreateRequestDto createActivityRequest = ActivityCreateRequestDto.builder()
				.name("Pagination Test Activity").description("Testing pagination with many participants")
				.location("Earth").startDateTime(now().plusDays(1)).endDateTime(now().plusDays(2)).build();
		createActivity(USERNAME1, createActivityRequest);

		ActivityDto activity = activityApi.getActivityByName("Pagination Test Activity", Pageable.unpaged()).get()
				.findFirst().orElseThrow();

		// Add 2 participants
		for (int i = 0; i < 2; i++) {
			String username = "user" + i;
			String email = username + "@bu.edu";
			System.out.println("Registering user: " + username + " with email: " + email);
			Users user = Users.builder().accountState(AccountState.VERIFIED).username(username).email(email)
					.password(PASSWORD).build();
			userRepository.save(user);
			activityApi.joinActivity(user.getUserId(), activity.getId());
		}

		// Page 0 with size 2
		Map<String, String> pathParams = Map.of("activityId", activity.getId());
		Map<String, String> pageParams = Map.of("page", "0", "size", "2");

		MvcResult page0Result = sendGet(mockMvc, PARTICIPANTS, pathParams, user1AccessToken, pageParams)
				.andExpect(status().isOk()).andReturn();

		JsonNode page0Json = objectMapper.readTree(page0Result.getResponse().getContentAsString()).at("/data");

		String nextPageUrl = page0Json.at("/nextPageUrl").asText();
		assertNotNull(nextPageUrl, "nextPageUrl should not be null");
		assertEquals(1, extractPageNumber(nextPageUrl), "Next page should be page 1");

		assertTrue(page0Json.at("/previousPageUrl").isNull(), "On page 0, previousPageUrl should not have any value");

		// GET page 1 using nextPageUrl
		MvcResult page1Result = mockMvc.perform(get(nextPageUrl).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + user1AccessToken)).andExpect(status().isOk()).andReturn();

		JsonNode page1Json = objectMapper.readTree(page1Result.getResponse().getContentAsString()).at("/data");
		JsonNode contentPage1 = page1Json.at("/content");

		assertNotNull(contentPage1);
		assertEquals(1, contentPage1.size(), "Page size should be 1");

		assertEquals(3, page1Json.at("/totalElements").asInt(),
				"Total elements should be 3. 2 participants and 1 admin");
		assertEquals(2, page1Json.at("/totalPages").asInt(), "Total pages should be 2");

		// Confirm correct next/prev links on page 1
		String prevPageUrlFrom1 = page1Json.at("/previousPageUrl").asText();
		assertTrue(page1Json.at("/nextPageUrl").isNull() || page1Json.at("/nextPageUrl").asText().isEmpty(),
				"Last page should not have nextPageUrl");
		assertEquals(0, extractPageNumber(prevPageUrlFrom1), "Should go back to page 0");
	}
}