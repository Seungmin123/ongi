package com.ongi.notification.service.adapter.out.external;

import com.ongi.grpc.user.UserContactRequest;
import com.ongi.grpc.user.UserContactResponse;
import com.ongi.grpc.user.UserInfoServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class UserGrpcClient {

	@GrpcClient("user-service")
	private UserInfoServiceGrpc.UserInfoServiceBlockingStub userInfoStub;

	public Mono<UserContactInfo> getUserContact(Long userId) {
		return Mono.fromCallable(() -> {
			UserContactRequest request = UserContactRequest.newBuilder()
				.setUserId(userId)
				.build();
			
			UserContactResponse response = userInfoStub.getUserContact(request);
			return new UserContactInfo(response.getPhoneNumber(), response.getPushToken());
		})
		.subscribeOn(Schedulers.boundedElastic());
	}

	public record UserContactInfo(String phoneNumber, String pushToken) {}
}
