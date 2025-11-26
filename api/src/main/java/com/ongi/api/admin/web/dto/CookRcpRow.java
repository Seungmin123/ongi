package com.ongi.api.admin.web.dto;

// 식품안전처 DTO
public record CookRcpRow(
	String RCP_NM, // 메뉴명
	String RCP_WAY2, // 조리방법 '찌기' '기타' '끓이기' 등
	String RCP_PAT2, // 요리종류 '반찬'
	String INFO_WGT, // 중량 '1인분'
	String INFO_ENG, // 열량
	String INFO_CAR, // 탄수화물
	String INFO_PRO, // 단백질
	String INFO_FAT, // 지방
	String INFO_NA, // 나트륨
	String HASH_TAG, // 해쉬태그
	String ATT_FILE_NO_MAIN, // 이미지경로(소)
	String ATT_FILE_NO_MK, // 이미지경로(대)
	String RCP_PARTS_DTLS, // 재료정보

	String RCP_NA_TIP, // 저감 조리법 TIP

	String MANUAL01,
	String MANUAL02,
	String MANUAL03,
	String MANUAL04,
	String MANUAL05,
	String MANUAL06,
	String MANUAL07,
	String MANUAL08,
	String MANUAL09,
	String MANUAL10,
	String MANUAL11,
	String MANUAL12,
	String MANUAL13,
	String MANUAL14,
	String MANUAL15,
	String MANUAL16,
	String MANUAL17,
	String MANUAL18,
	String MANUAL19,
	String MANUAL20,

	String MANUAL_IMG01,
	String MANUAL_IMG02,
	String MANUAL_IMG03,
	String MANUAL_IMG04,
	String MANUAL_IMG05,
	String MANUAL_IMG06,
	String MANUAL_IMG07,
	String MANUAL_IMG08,
	String MANUAL_IMG09,
	String MANUAL_IMG10,
	String MANUAL_IMG11,
	String MANUAL_IMG12,
	String MANUAL_IMG13,
	String MANUAL_IMG14,
	String MANUAL_IMG15,
	String MANUAL_IMG16,
	String MANUAL_IMG17,
	String MANUAL_IMG18,
	String MANUAL_IMG19,
	String MANUAL_IMG20
) {}
