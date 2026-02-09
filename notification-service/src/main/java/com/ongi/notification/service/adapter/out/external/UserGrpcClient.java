package com.ongi.notification.service.adapter.out.external;

import com.ongi.grpc.user.StreamUsersRequest;
import com.ongi.grpc.user.UserContactRequest;
import com.ongi.grpc.user.UserContactResponse;
import com.ongi.grpc.user.UserInfoServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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

			userInfoStub.getUserContact(request, new StreamObserver<>() {
				@Override
				public void onNext(UserContactResponse value) {
					sink.success(new UserContactInfo(value.getUserId(), value.getPhoneNumber(), value.getPushToken()));
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

	public Flux<UserContactInfo> streamUsers(String targetGroup) {
		return Flux.create(sink -> {
			StreamUsersRequest request = StreamUsersRequest.newBuilder()
				.setTargetGroup(targetGroup)
				.build();

			userInfoStub.streamUsers(request, new StreamObserver<>() {
				@Override
				public void onNext(UserContactResponse value) {
					sink.next(new UserContactInfo(value.getUserId(), value.getPhoneNumber(), value.getPushToken()));
				}

				@Override
				public void onError(Throwable t) {
					sink.error(t);
				}

				@Override
				public void onCompleted() {
					sink.complete();
				}
			});
		});
	}

	public record UserContactInfo(Long userId, String phoneNumber, String pushToken) {}
}
