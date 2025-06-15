package com.bu.getactivecore.model.users;

/**
 * Represents the state of a user's account.
 */
public enum AccountState {
	/**
	 * This is the default state when user registers an account and remain in this
	 * state until they verify their email address. In this state, the user is not
	 * allowed to access the secured REST APIs. Once the user verifies their email
	 * address, they will be transitioned to the VERIFIED state.
	 */
	UNVERIFIED,

	/**
	 * The account is verified, indicating that the user has confirmed their email
	 * address. In this state, the user is allowed to access the secured REST APIs.
	 */
	VERIFIED,
}