package com.ongi.api.user.application.component;

public interface MailSender {

	void sendEmailVerificationCode(String email, String code);

	void sendUserIdGuideMail(String email);

	void sendPasswordResetMail(String email, String resetLinkOrToken);

}
