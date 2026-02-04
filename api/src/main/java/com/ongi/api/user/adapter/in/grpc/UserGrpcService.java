package com.ongi.api.user.adapter.in.grpc;

import com.ongi.grpc.user.UserContactRequest;
import com.ongi.grpc.user.UserContactResponse;
import com.ongi.grpc.user.UserInfoServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
public class UserGrpcService extends UserInfoServiceGrpc.UserInfoServiceImplBase {

	@Override
	public void getUserContact(UserContactRequest request, StreamObserver<UserContactResponse> responseObserver) {
		log.info("[gRPC Server] 유저 정보 요청 수신: userId={}", request.getUserId());

		// 실제 유저 DB 조회 로직 (시뮬레이션)
		UserContactResponse response = UserContactResponse.newBuilder()
			.setUserId(request.getUserId())
			.setPhoneNumber("010-1234-5678")
			.setPushToken("fcm-token-grpc")
			.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
