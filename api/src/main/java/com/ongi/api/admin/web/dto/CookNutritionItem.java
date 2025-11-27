package com.ongi.api.admin.web.dto;

public record CookNutritionItem(
	// 번호
	String NUM,

	// 식품코드
	String FOOD_CD,

	// 식품명
	String FOOD_NM_KR,

	// 데이터구분코드
	String DB_GRP_CM,

	// 데이터구분명
	String DB_GRP_NM,

	// 품목대표/상용제품 코드
	String DB_CLASS_CM,

	// 품목대표/상용제품
	String DB_CLASS_NM,

	// 식품기원코드
	String FOOD_OR_CD,

	// 식품기원명
	String FOOD_OR_NM,

	// 식품대분류코드
	String FOOD_CAT1_CD,

	// 식품대분류명
	String FOOD_CAT1_NM,

	// 대표식품코드
	String FOOD_REF_CD,

	// 대표식품명
	String FOOD_REF_NM,

	// 식품중분류코드
	String FOOD_CAT2_CD,

	// 식품중분류명
	String FOOD_CAT2_NM,

	// 식품소분류코드
	String FOOD_CAT3_CD,

	// 식품소분류명
	String FOOD_CAT3_NM,

	// 식품세분류코드
	String FOOD_CAT4_CD,

	// 식품세분류명
	String FOOD_CAT4_NM,

	// 영양성분함량기준량
	String SERVING_SIZE,

	// 에너지(kcal)
	String AMT_NUM1,

	// 수분(g)
	String AMT_NUM2,

	// 단백질(g)
	String AMT_NUM3,

	// 지방(g)
	String AMT_NUM4,

	// 회분(g)
	String AMT_NUM5,

	// 탄수화물(g)
	String AMT_NUM6,

	// 당류(g)
	String AMT_NUM7,

	// 식이섬유(g)
	String AMT_NUM8,

	// 칼슘(mg)
	String AMT_NUM9,

	// 철(mg)
	String AMT_NUM10,

	// 인(mg)
	String AMT_NUM11,

	// 칼륨(mg)
	String AMT_NUM12,

	// 나트륨(mg)
	String AMT_NUM13,

	// 비타민 A(μg RAE)
	String AMT_NUM14,

	// 비타민 A(μg)
	String AMT_NUM15,

	// 레티놀(μg)
	String AMT_NUM16,

	// 베타카로틴(μg)
	String AMT_NUM17,

	// 비타민 B1(mg)
	String AMT_NUM18,

	// 비타민 B2(mg)
	String AMT_NUM19,

	// 니아신(mg)
	String AMT_NUM20,

	// 비타민 C(mg)
	String AMT_NUM21,

	// 비타민 D(μg)
	String AMT_NUM22,

	// 콜레스테롤(mg)
	String AMT_NUM23,

	// 포화지방산(g)
	String AMT_NUM24,

	// 트랜스지방산(g)
	String AMT_NUM25,

	// 니코틴산 (mg)
	String AMT_NUM26,

	// 니코틴아마이드(mg)
	String AMT_NUM27,

	// 비오틴(μg)
	String AMT_NUM28,

	// 비타민 B6 (mg)
	String AMT_NUM29,

	// 비타민 B12(μg)
	String AMT_NUM30,

	// 엽산(DFE)(㎍)
	String AMT_NUM31,

	// 콜린(mg)
	String AMT_NUM32,

	// 판토텐산(mg)
	String AMT_NUM33,

	// 비타민 D2(μg)
	String AMT_NUM34,

	// 비타민 D3(μg)
	String AMT_NUM35,

	// 비타민 E(mg α-TE)
	String AMT_NUM36,

	// 비타민 E(mg)
	String AMT_NUM37,

	// 토코페롤(㎎)
	String AMT_NUM38,

	// 알파 토코페롤(mg)
	String AMT_NUM39,

	// 베타 토코페롤(mg)
	String AMT_NUM40,

	// 감마 토코페롤(mg)
	String AMT_NUM41,

	// 델타 토코페롤(mg)
	String AMT_NUM42,

	// 토코트리에놀(㎎)
	String AMT_NUM43,

	// 알파 토코트리에놀(mg)
	String AMT_NUM44,

	// 베타 토코트리에놀(mg)
	String AMT_NUM45,

	// 감마 토코트리에놀(mg)
	String AMT_NUM46,

	// 델타 토코트리에놀(mg)
	String AMT_NUM47,

	// 비타민 K(μg)
	String AMT_NUM48,

	// 비타민 K1(μg)
	String AMT_NUM49,

	// 비타민 K2(μg)
	String AMT_NUM50,

	// 갈락토오스(g)
	String AMT_NUM51,

	// 과당(g)
	String AMT_NUM52,

	// 당알콜(g)
	String AMT_NUM53,

	// 맥아당(g)
	String AMT_NUM54,

	// 알룰로오스(g)
	String AMT_NUM55,

	// 에리스리톨(g)
	String AMT_NUM56,

	// 유당(g)
	String AMT_NUM57,

	// 자당(g)
	String AMT_NUM58,

	// 타가토스(g)
	String AMT_NUM59,

	// 포도당(g)
	String AMT_NUM60,

	// 총 불포화지방산(g)
	String AMT_NUM61,

	// EPA와 DHA의 합(mg)
	String AMT_NUM62,

	// 가돌레산/에이코센산(mg)
	String AMT_NUM63,

	// 감마 리놀렌산(18:3 n-6)(mg)
	String AMT_NUM64,

	// 네르본산(24:1)(mg)
	String AMT_NUM65,

	// 도코사디에노산(22:2)(mg)
	String AMT_NUM66,

	// 도코사펜타에노산(22:5(n-3))(mg)
	String AMT_NUM67,

	// 도코사펜타엔산(n-6) (22:5,n-6)(mg)
	String AMT_NUM68,

	// 도코사헥사에노산(22:6(n-3))(mg)
	String AMT_NUM69,

	// 디호모리놀렌산(20:3(n-3))(mg)
	String AMT_NUM70,

	// 디호모감마리놀렌산(20:3,n-6))(mg)
	String AMT_NUM71,

	// 라우르산(12:0)(mg)
	String AMT_NUM72,

	// 리그노세르산(24:0)(mg)
	String AMT_NUM73,

	// 리놀레산(18:2(n-6)c)(g)
	String AMT_NUM74,

	// 리놀레산(18:2(n-6)c)(mg)
	String AMT_NUM75,

	// 미리스톨레산(14:1)(mg)
	String AMT_NUM76,

	// 미리스트산(14:0)(mg)
	String AMT_NUM77,

	// 박센산(18:1(n-7))(mg)
	String AMT_NUM78,

	// 베헨산(22:0)(mg)
	String AMT_NUM79,

	// 부티르산(4:0)(mg)
	String AMT_NUM80,

	// 스테아르산(18:0)(mg)
	String AMT_NUM81,

	// 스테아리돈산(18:4)(mg)
	String AMT_NUM82,

	// 아라키돈산(20:4 n-6)(mg)
	String AMT_NUM83,

	// 아라키드산(20:0)(mg)
	String AMT_NUM84,

	// 알파 리놀렌산(18:3(n-3))(g)
	String AMT_NUM85,

	// 알파 리놀렌산(18:3(n-3))(mg)
	String AMT_NUM86,

	// 에루크산(22:1)(mg)
	String AMT_NUM87,

	// 에이코사디에노산(20:2(n-6))(mg)
	String AMT_NUM88,

	// 에이코사트리에노산(20:3(n-6))(mg)
	String AMT_NUM89,

	// 에이코사펜타에노산(20:5(n-3))(mg)
	String AMT_NUM90,

	// 오메가3 지방산(g)
	String AMT_NUM91,

	// 오메가6 지방산(g)
	String AMT_NUM92,

	// 올레산(18:1 n-9)(mg)
	String AMT_NUM93,

	// 카프로산(6:0)(mg)
	String AMT_NUM94,

	// 카프르산(10:0)(mg)
	String AMT_NUM95,

	// 카프릴산(8:0)(mg)
	String AMT_NUM96,

	// 트라이데칸산(13:0)(mg)
	String AMT_NUM97,

	// 트랜스 리놀레산(18:2t)(mg)
	String AMT_NUM98,

	// 트랜스 리놀렌산(18:3t)(mg)
	String AMT_NUM99,

	// 카페인(㎎)
	String AMT_NUM100,

	// 트랜스 올레산(18:1(n-9)t)(mg)
	String AMT_NUM101,

	// 트리코산산(23:0)(mg)
	String AMT_NUM102,

	// 팔미톨레산(16:1)(mg)
	String AMT_NUM103,

	// 팔미트산(16:0)(mg)
	String AMT_NUM104,

	// 펜타데칸산(15:0)(mg)
	String AMT_NUM105,

	// 헨에이코산산(21:0)(mg)
	String AMT_NUM106,

	// 헵타데센산(17:1)(mg)
	String AMT_NUM107,

	// 헵타데칸산(17:0)(mg)
	String AMT_NUM108,

	// 구리(㎎)
	String AMT_NUM109,

	// 구리(μg)
	String AMT_NUM110,

	// 마그네슘(mg)
	String AMT_NUM111,

	// 망간(mg)
	String AMT_NUM112,

	// 몰리브덴(μg)
	String AMT_NUM113,

	// 불소(mg)
	String AMT_NUM114,

	// 셀레늄(μg)
	String AMT_NUM115,

	// 아연(mg)
	String AMT_NUM116,

	// 염소(mg)
	String AMT_NUM117,

	// 요오드(μg)
	String AMT_NUM118,

	// 크롬(μg)
	String AMT_NUM119,

	// 총 아미노산(mg)
	String AMT_NUM120,

	// 필수아미노산(mg)
	String AMT_NUM121,

	// 비필수아미노산(mg)
	String AMT_NUM122,

	// 글루탐산(mg)
	String AMT_NUM123,

	// 글리신(mg)
	String AMT_NUM124,

	// 라이신(mg)
	String AMT_NUM125,

	// 루신(mg)
	String AMT_NUM126,

	// 메티오닌(mg)
	String AMT_NUM127,

	// 발린(mg)
	String AMT_NUM128,

	// 세린(mg)
	String AMT_NUM129,

	// 시스테인(mg)
	String AMT_NUM130,

	// 아르기닌(mg)
	String AMT_NUM131,

	// 아스파르트산(mg)
	String AMT_NUM132,

	// 알라닌(mg)
	String AMT_NUM133,

	// 이소루신(mg)
	String AMT_NUM134,

	// 타우린(mg)
	String AMT_NUM135,

	// 트레오닌(mg)
	String AMT_NUM136,

	// 트립토판(mg)
	String AMT_NUM137,

	// 티로신(mg)
	String AMT_NUM138,

	// 페닐알라닌(mg)
	String AMT_NUM139,

	// 프롤린(mg)
	String AMT_NUM140,

	// 히스티딘(mg)
	String AMT_NUM141,

	// 펜타데센산(15:1,n-5)(mg)
	String AMT_NUM142,

	// 에이코사테트라에노산(20:4(n-3))
	String AMT_NUM143,

	// 헤니코사펜타엔산(21:5,n-3)(mg)
	String AMT_NUM144,

	// 니아신당량(NE)
	String AMT_NUM145,

	// 수용성 식이섬유(g)
	String AMT_NUM146,

	// 불용성 식이섬유(g)
	String AMT_NUM147,

	// 피리독신(mg)
	String AMT_NUM148,

	// 엽산_식품 엽산(μg)
	String AMT_NUM149,

	// 엽산_합성 엽산(μg)
	String AMT_NUM150,

	// 총 필수지방산(g)
	String AMT_NUM151,

	// 총 단일불포화지방산(g)
	String AMT_NUM152,

	// 총 다중불포화지방산(g)
	String AMT_NUM153,

	// 총 지방산(g)
	String AMT_NUM154,

	// 지방산의 합(g)
	String AMT_NUM155,

	// 식염상당량(g)
	String AMT_NUM156,

	// 폐기율(%)
	String AMT_NUM157,

	// 출처명
	String SUB_REF_NAME,

	// 1회 섭취참고량
	String NUTRI_AMOUNT_SERVING,

	// 식품중량
	String Z10500,

	// 1회분량 참고량
	String DISH_ONE_SERVING,

	// 품목제조보고번호
	String ITEM_REPORT_NO,

	// 업체명
	String MAKER_NM,

	// 수입업체명
	String IMP_MANUFAC_NM,

	// 유통업체명
	String SELLER_MANUFAC_NM,

	// 수입여부
	String IMP_YN,

	// 원산지국코드
	String NATION_CM,

	// 원산지국명
	String NATION_NM,

	// 데이터생성방법코드
	String CRT_MTH_CD,

	// 데이터생성방법명
	String CRT_MTH_NM,

	// 출처코드
	String SUB_REF_CM,

	// 데이터생성일자
	String RESEARCH_YMD,

	// 데이터수정일자
	String UPDATE_DATE
) {}
