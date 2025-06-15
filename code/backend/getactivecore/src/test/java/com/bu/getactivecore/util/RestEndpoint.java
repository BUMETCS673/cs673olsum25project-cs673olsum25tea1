package com.bu.getactivecore.util;

/**
 * Helper enum which contains REST API endpoints used in tests.
 */
public enum RestEndpoint {
	ACTIVITY("/v1/activity"), CONFIRM_REGISTRATION("/v1/register/confirmation"), LOGIN("/v1/login"),
	REGISTER("/v1/register"), PARTICIPANTS("/v1/activities/{activityId}/participants"),
	RESEND_CONFIRMATION("/v1/register/confirmation/resend");

	private final String endpoint;

	RestEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String get() {
		return endpoint;
	}
}
