package com.hanium.mom4u.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


/**
 * 로그 객체에 대한 JSON으로 변경하여 가독성 업그레이드
 */
@Slf4j
@Component
public class LogUtil {

    // 예외 객체 정돈
    public static String exception(Throwable e) {
        return e == null ? "" : StringUtils.normalizeSpace(e.toString());
    }

    // DEBUG LOG
    public static void debug(String msg, Object... args) {
        if (log.isDebugEnabled()) {log.debug(msg, args);}
    }

    // INFO LOG
    public static void info(String msg, Object... args) {
        if (log.isInfoEnabled()) log.info(msg, args);
    }

    // ERROR LOG
    public static void error(String msg, Object... args) {
        if (log.isErrorEnabled()) log.error(msg, args);
    }

    // Request ERROR LOG
    public static void error(Throwable e, HttpServletRequest request) {
        if (request == null) {
            log.error("Exception : {}", exception(e), e);
            return;
        }
        log.error("Request URI : [{}] {}", request.getMethod(), request.getRequestURI());
        log.error("Exception : ", e);
        log.error(e.getMessage());
    }

}
