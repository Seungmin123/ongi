package com.ongi.notification.service.adapter.out.external;

import com.ongi.grpc.user.UserContactRequest;
import com.ongi.grpc.user.UserContactResponse;
import com.ongi.grpc.user.UserInfoServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserGrpcClient {

	@GrpcClient("user-service")
	private UserInfoServiceGrpc.UserInfoServiceStub userInfoStub;

	public Mono<UserContactInfo> getUserContact(Long userId) {
		return Mono.create(sink -> {
			UserContactRequest request = UserContactRequest.newBuilder()
				.setUserId(userId)
				.build();

			userInfoStub.getUserContact(request, new StreamObserver<UserContactResponse>() {
				@Override
				public void onNext(UserContactResponse value) {
					sink.success(new UserContactInfo(value.getPhoneNumber(), value.getPushToken()));
				}

				@Override
				public void onError(Throwable t) {
					sink.error(t);
				}

				@Override
				public void onCompleted() {
					// Unary call 완료 (별도 처리 불필요)
				}
			});
		});
	}

	public record UserContactInfo(String phoneNumber, String pushToken) {}
}
