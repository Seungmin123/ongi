package com.ongi.api.user.adapter.in.grpc;

import com.ongi.api.user.adapter.out.persistence.UserEntity;
import com.ongi.api.user.adapter.out.persistence.repository.UserRepository;
import com.ongi.grpc.user.StreamUsersRequest;
import com.ongi.grpc.user.UserContactRequest;
import com.ongi.grpc.user.UserContactResponse;
import com.ongi.grpc.user.UserInfoServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserInfoServiceGrpc.UserInfoServiceImplBase {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public void getUserContact(UserContactRequest request, StreamObserver<UserContactResponse> responseObserver) {
		log.info("[gRPC Server] 유저 정보 요청 수신: userId={}", request.getUserId());

		userRepository.findById(request.getUserId())
			.ifPresentOrElse(user -> {
				UserContactResponse response = UserContactResponse.newBuilder()
					.setUserId(user.getId())
					.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
					.setPushToken(user.getPushToken() != null ? user.getPushToken() : "")
					.build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			}, () -> {
				log.warn("유저를 찾을 수 없음: userId={}", request.getUserId());
				responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
			});
	}

	@Override
	@Transactional(readOnly = true)
	public void streamUsers(StreamUsersRequest request, StreamObserver<UserContactResponse> responseObserver) {
		log.info("[gRPC Server] 유저 스트리밍 요청 수신: group={}", request.getTargetGroup());

		Stream<UserEntity> userStream;

		if ("MARKETING_AGREED".equals(request.getTargetGroup())) {
			userStream = userRepository.streamAllByMarketingAgreedTrue();
		} else {
			userStream = userRepository.streamAll();
		}

		// try-with-resources로 스트림 자동 종료 보장 (DB 커넥션 반환)
		try (userStream) {
			userStream.forEach(user -> {
				UserContactResponse response = UserContactResponse.newBuilder()
					.setUserId(user.getId())
					.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
					.setPushToken(user.getPushToken() != null ? user.getPushToken() : "")
					.build();

				responseObserver.onNext(response);
			});

			log.info("[gRPC Server] 유저 스트리밍 완료");
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.error("[gRPC Server] 스트리밍 중 에러 발생", e);
			responseObserver.onError(e);
		}
	}
}
