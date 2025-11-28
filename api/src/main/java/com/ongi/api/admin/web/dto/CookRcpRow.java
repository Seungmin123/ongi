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
) {
	public String getManual(int i) {
		return switch (i) {
			case 1 -> MANUAL01();
			case 2 -> MANUAL02();
			case 3 -> MANUAL03();
			case 4 -> MANUAL04();
			case 5 -> MANUAL05();
			case 6 -> MANUAL06();
			case 7 -> MANUAL07();
			case 8 -> MANUAL08();
			case 9 -> MANUAL09();
			case 10 -> MANUAL10();
			case 11 -> MANUAL11();
			case 12 -> MANUAL12();
			case 13 -> MANUAL13();
			case 14 -> MANUAL14();
			case 15 -> MANUAL15();
			case 16 -> MANUAL16();
			case 17 -> MANUAL17();
			case 18 -> MANUAL18();
			case 19 -> MANUAL19();
			case 20 -> MANUAL20();
			default -> null;
		};
	}

	public String getManualImg(int i) {
		return switch (i) {
			case 1 -> MANUAL_IMG01();
			case 2 -> MANUAL_IMG02();
			case 3 -> MANUAL_IMG03();
			case 4 -> MANUAL_IMG04();
			case 5 -> MANUAL_IMG05();
			case 6 -> MANUAL_IMG06();
			case 7 -> MANUAL_IMG07();
			case 8 -> MANUAL_IMG08();
			case 9 -> MANUAL_IMG09();
			case 10 -> MANUAL_IMG10();
			case 11 -> MANUAL_IMG11();
			case 12 -> MANUAL_IMG12();
			case 13 -> MANUAL_IMG13();
			case 14 -> MANUAL_IMG14();
			case 15 -> MANUAL_IMG15();
			case 16 -> MANUAL_IMG16();
			case 17 -> MANUAL_IMG17();
			case 18 -> MANUAL_IMG18();
			case 19 -> MANUAL_IMG19();
			case 20 -> MANUAL_IMG20();
			default -> null;
		};
	}
}
