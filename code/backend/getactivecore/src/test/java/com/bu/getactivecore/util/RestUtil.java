package com.bu.getactivecore.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;

import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for performing REST API operations during tests.
 */
public class RestUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	/**
	 * Sends a POST request to the specified endpoint.
	 *
	 * @param mockMvc  the MockMvc instance to perform the request
	 * @param endpoint the REST endpoint to send the request to
	 * @param request  the request body as a Map
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendPost(MockMvc mockMvc, RestEndpoint endpoint, Object request) throws Exception {
		return mockMvc.perform(post(endpoint.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));
	}

	/**
	 * Send a POST request to the specified endpoint.
	 *
	 * @param mockMvc  the MockMvc instance to perform the request
	 * @param endpoint the REST endpoint to send the request to
	 * @param request  the request body as a Map
	 * @param token    the authentication token to include in the request header
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendPost(MockMvc mockMvc, RestEndpoint endpoint, Map<String, String> request,
			String token) throws Exception {
		return mockMvc.perform(post(endpoint.get()).header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
	}

	/**
	 * Sends a GET request to the specified endpoint.
	 *
	 * @param mockMvc  the MockMvc instance to perform the request
	 * @param endpoint the REST endpoint to send the request to
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendGet(MockMvc mockMvc, RestEndpoint endpoint) throws Exception {
		return mockMvc.perform(post(endpoint.get()).contentType(MediaType.APPLICATION_JSON));
	}

	/**
	 * Sends a GET request to the specified endpoint with the given data.
	 *
	 * @param mockMvc  the MockMvc instance to perform the request
	 * @param endpoint the REST endpoint to send the request to
	 * @param data     the request body
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendGet(MockMvc mockMvc, RestEndpoint endpoint, Object data) throws Exception {
		return mockMvc.perform(get(endpoint.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(data)));
	}

	/**
	 * Sends a GET request to the specified endpoint with path parameters.
	 *
	 * @param mockMvc    the MockMvc instance to perform the request
	 * @param endpoint   the REST endpoint to send the request to
	 * @param pathParams a map of path parameters to replace in the endpoint URL
	 *
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendGet(MockMvc mockMvc, RestEndpoint endpoint, Map<String, String> pathParams,
			String token) throws Exception {
		return sendGet(mockMvc, endpoint, pathParams, token, null);
	}

	public static ResultActions sendGet(MockMvc mockMvc, RestEndpoint endpoint, Map<String, String> pathParams,
			String token, Map<String, String> queryParams) throws Exception {
		// Replace path parameters in endpoint
		String resolvedPath = endpoint.get();
		for (Map.Entry<String, String> entry : pathParams.entrySet()) {
			String placeholder = "{" + entry.getKey() + "}";
			resolvedPath = resolvedPath.replace(placeholder, entry.getValue());
		}

		// Add query parameters to URI
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(resolvedPath);
		if (queryParams != null) {
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				uriBuilder.queryParam(entry.getKey(), entry.getValue());
			}
		}

		String finalUrl = uriBuilder.toUriString();

		return mockMvc.perform(
				get(finalUrl).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON));
	}

	/**
	 * Sends a PUT request to the specified endpoint with the given request body.
	 *
	 * @param mockMvc  the MockMvc instance to perform the request
	 * @param endpoint the REST endpoint to send the request to
	 * @param request  the request body as a Map
	 * @param token    the authentication token to include in the request header
	 * @return ResultActions containing the result of the performed action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions sendPut(MockMvc mockMvc, RestEndpoint endpoint, Object request, String token)
			throws Exception {
		return mockMvc.perform(put(endpoint.get()).header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
	}

	/**
	 * Registers and logs in the user to obtain a JWT token.
	 *
	 * @param mockMvc         the MockMvc instance to perform the request
	 * @param registerRequest the registration request containing user details
	 * @return the JWT token as a String
	 * @throws Exception if an error occurs during request execution
	 */
	public static String getToken(MockMvc mockMvc, RegistrationRequestDto registerRequest) throws Exception {
		MvcResult response = registerAndLogin(mockMvc, registerRequest).andReturn();
		JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
		return jsonNode.at("/data/token").asText();
	}

	/**
	 * Registers and logs in a user.
	 *
	 * @param mockMvc         the MockMvc instance to perform the request
	 * @param registerRequest the registration request containing user details
	 * @return ResultActions containing the result of the login action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions registerAndLogin(MockMvc mockMvc, RegistrationRequestDto registerRequest)
			throws Exception {
		// Register the user first
		MvcResult response = register(mockMvc, registerRequest).andExpect(status().isOk()).andReturn();

		// Confirm the registration
		JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
		String confirmationToken = jsonNode.at("/data/token").asText();

		confirmRegistration(mockMvc, new ConfirmationRequestDto(confirmationToken)).andExpect(status().isOk());

		// Then log in with the registered user
		LoginRequestDto loginRequest = new LoginRequestDto(registerRequest.getUsername(),
				registerRequest.getPassword());
		return login(mockMvc, loginRequest);
	}

	/**
	 * Registers a user with the provided registration request.
	 *
	 * @param mockMvc         the MockMvc instance to perform the request
	 * @param registerRequest the registration request containing user details
	 * @return ResultActions containing the result of the registration action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions register(MockMvc mockMvc, RegistrationRequestDto registerRequest) throws Exception {
		return mockMvc.perform(post(RestEndpoint.REGISTER.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)));
	}

	/**
	 * Logs in a user with the provided login request.
	 *
	 * @param mockMvc      the MockMvc instance to perform the request
	 * @param loginRequest the login request
	 * @return ResultActions containing the result of the login action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions login(MockMvc mockMvc, Object loginRequest) throws Exception {
		return mockMvc.perform(post(RestEndpoint.LOGIN.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));
	}

	/**
	 * Sends a confirmation request to verify user registration.
	 *
	 * @param mockMvc             the MockMvc instance to perform the request
	 * @param confirmationRequest the confirmation request containing the token
	 * @return ResultActions containing the result of the confirmation action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions confirmRegistration(MockMvc mockMvc, Object confirmationRequest) throws Exception {
		return mockMvc.perform(post(RestEndpoint.CONFIRM_REGISTRATION.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(confirmationRequest)));
	}

	/**
	 * Resends the confirmation registration request.
	 *
	 * @param mockMvc             the MockMvc instance to perform the request
	 * @param confirmationRequest the confirmation request containing user details
	 * @return ResultActions containing the result of the resend action
	 * @throws Exception if an error occurs during request execution
	 */
	public static ResultActions resendConfirmRegistration(MockMvc mockMvc, Object confirmationRequest)
			throws Exception {
		return mockMvc.perform(post(RestEndpoint.RESEND_CONFIRMATION.get()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(confirmationRequest)));
	}
}