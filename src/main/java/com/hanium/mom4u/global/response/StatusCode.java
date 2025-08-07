package com.hanium.mom4u.global.response;

import org.springframework.http.HttpStatus;

public enum StatusCode {

    // test
    FAILURE_TEST(HttpStatus.INTERNAL_SERVER_ERROR, "TESTERROR", "테스트 실패에 대한 응답입니다."),

    // token
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TE4001", "토큰을 찾을 수 없습니다."),
    EMPTY_COOKIE(HttpStatus.NOT_FOUND, "TE4002", "쿠키가 비어있습니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "TE4003", "유효하지 않은 JWT 토큰입니다."),
    REFRESH_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TE4004", "쿠키에 refresh token이 존재하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "TE4005", "인증된 접근 방식이 아닙니다."),

    //kakao login
    KAKAO_AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "KAKAO320", "카카오 인가 코드가 이미 사용되었거나 만료되었습니다."),
    KAKAO_REDIRECT_URI_MISMATCH(HttpStatus.BAD_REQUEST, "KAKAO303", "카카오 redirect_uri가 일치하지 않습니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "KAKAO401", "카카오 로그인에 실패했습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TE4002", "유효하지 않은 토큰입니다."),
    // 카카오 에러 확장
    KAKAO_CLIENT_INVALID(HttpStatus.UNAUTHORIZED, "KAKAO101", "잘못된 클라이언트 정보입니다"),
    KAKAO_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "KAKAO102", "잘못된 요청 형식입니다"),
    KAKAO_REDIRECT_URI_INVALID(HttpStatus.BAD_REQUEST, "KAKAO006", "등록되지 않은 Redirect URI입니다"),
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO500", "카카오 서버 오류가 발생했습니다"),

    // Naver
    NAVER_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "NE4001", "네이버 서버로부터 토큰을 읽어들이는 데에 실패하였습니다."),
    NAVER_PROFILE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "NE4002", "네이버 서버로부터 프로필 정보 요청에 실패하였습니다."),
    NAVER_PROFILE_FAILED(HttpStatus.BAD_REQUEST, "NE4003", "네이버로부터 프로필 정보를 읽어들이는 데에 실패하였습니다."),
    NAVER_PARSE_ERROR(HttpStatus.BAD_REQUEST, "NE4004", "네이버로부터 얻은 정보를 파싱하는 데에 실패하였습니다."),

    // Google
    GOOGLE_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "GE4001", "구글 서버로부터 토큰을 읽어들이는 데에 실패하였습니다."),
    GOOGLE_PROFILE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "GE4002", "구글 서버로부터 프로필 정보 요청에 실패하였습니다."),
    GOOGLE_PROFILE_FAILED(HttpStatus.BAD_REQUEST, "GE4003", "구글 프로필 정보를 읽어들이는 데에 실패하였습니다."),
    GOOGLE_PARSE_ERROR(HttpStatus.BAD_REQUEST, "GE4004", "구글로부터 얻은 정보를 파싱하는 데에 실패하였습니다."),


    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ME4001", "회원을 조회할 수 없습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE5001", "파일 저장에 실패했습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE4002", "파일이 비어있습니다."),


    //code
    DUPLICATE_FAMILY_CODE(HttpStatus.CONFLICT, "FE4002", "이미 사용 중인 가족 코드입니다."),
    INVALID_FAMILY_CODE(HttpStatus.BAD_REQUEST, "FAM4001", "유효하지 않거나 만료된 가족 코드입니다."),
    FORBIDDEN_FAMILY_CODE_CREATION(HttpStatus.FORBIDDEN, "FAM4002", "임산부만 가족 코드를 생성할 수 있습니다."),
    ALREADY_IN_FAMILY(HttpStatus.BAD_REQUEST, "FAM4004", "이미 가족에 속해 있어 가족 코드를 생성할 수 없습니다."),
    NOT_IN_FAMILY(HttpStatus.FORBIDDEN, "FAM4005", "가족에 속해 있지 않습니다."),


    // schedule
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SC4001", "일정을 찾을 수 없습니다."),
    SCHEDULE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SC4002", "일정은 최대 10개까지만 등록할 수 있습니다."),

    // baby
    ONLY_PREGNANT(HttpStatus.UNAUTHORIZED, "BE4001", "임산부에게 주어진 권한입니다."),
    BABY_NOT_FOUND(HttpStatus.BAD_REQUEST, "BE4002", "해당 태아를 조회할 수 없습니다."),

    // family
    UNREGISTERED_FAMILY(HttpStatus.NOT_FOUND, "FE4001", "잘못된 인증 코드입니다."),

    // server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER5001", "서버에서 에러가 발생했습니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "JSON400", "JSON 형식 오류"),
    NETWORK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NET500", "네트워크 오류 발생"),
    JSON_PARSING_ERROR(HttpStatus.BAD_REQUEST, "JSON401", "Json 파싱 실패"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String description;

    StatusCode(HttpStatus httpStatus, String code, String description) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.description = description;
    }

    public HttpStatus getHttpStatus() { return this.httpStatus; }
    public String getCode() { return this.code; }
    public String getDescription() { return this.description; }
}
