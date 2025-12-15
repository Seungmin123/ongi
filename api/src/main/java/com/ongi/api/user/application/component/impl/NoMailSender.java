package com.ongi.api.user.application.component.impl;

import com.ongi.api.user.application.component.MailSender;
import org.springframework.stereotype.Component;

// TODO
@Component
public class NoMailSender implements MailSender {

	@Override
	public void sendEmailVerificationCode(String email, String code) {

	}

	@Override
	public void sendUserIdGuideMail(String email) {

	}

	@Override
	public void sendPasswordResetMail(String email, String resetLinkOrToken) {

	}

}
