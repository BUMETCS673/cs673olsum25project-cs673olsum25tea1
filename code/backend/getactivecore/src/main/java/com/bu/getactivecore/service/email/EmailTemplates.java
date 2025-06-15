package com.bu.getactivecore.service.email;

/**
 * Currently, there is only one email template for the registration verification
 * email.
 */
public class EmailTemplates {

	/**
	 * Template for the email verification message. '%s' will be replaced with the
	 * verification link.
	 */
	public static final String REGISTRATION_TEMPLATE = """
			Welcome to GetActive! 🎉

			Thanks for registering with us. To complete your registration, please enter the following verification token in the app:

			Your verification token: %s

			This step ensures we’ve got the right email and lets you fully access your GetActive account.

			If you didn't sign up for GetActive, you can safely ignore this message.

			Thank you
			""";
}
