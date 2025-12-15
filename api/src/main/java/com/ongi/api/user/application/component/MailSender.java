package com.ongi.api.user.application.component;

public interface MailSender {

	void sendUserIdGuideMail(String email);

	void sendPasswordResetMail(String email, String resetLinkOrToken);

}
