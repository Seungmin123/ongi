// TODO

//package com.ongi.api.config.advice;
//
//import com.muzlive.billboard.service.transfer.WebhookTransfer;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ExceptionReporter {
//
//	private final WebhookTransfer webhookTransfer; // 웹훅 전송 클래스
//
//	public void report(Throwable ex) {
//		String message = String.format("Unhandled Exception: %s - %s", ex.getClass().getName(), ex.getMessage());
//		webhookTransfer.sendSlackMessage(message);
//		log.error("예외 발생", ex);
//	}
//}