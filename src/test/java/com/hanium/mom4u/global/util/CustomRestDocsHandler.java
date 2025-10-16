package com.hanium.mom4u.global.util;

import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

/**
 * REST Docs 문서를 위한 커스텀
 * 스니펫 생성 위치 : build/generated-snippets/{class-name}/{identifier}/...
 */
public interface CustomRestDocsHandler {

    //private CustomRestDocsHandler() {}

    /**
     *  가장 기본이 되는 문서화 : 기본 전처리(Pretty Print) + {class-name}/{identifier}/... 패턴
     */
    public static RestDocumentationResultHandler customDocument(
            final String identifier,
            final Snippet... snippets) {

        return document("{class-name}/" + identifier,
                getDocumentRequest(),
                getDocumentResponse(),
                snippets);
    }

    /**
     * 기본 문서화 + 헤더가 존재할 경우
     */
    public static RestDocumentationResultHandler customDocument(
            final String identifier,
            final String[] sensitiveRequestHeadersToRemove,
            final String[] sensitiveResponseHeadersToRemove,
            final Snippet... snippets) {

        return document("{class-name}/" + identifier,
                getDocumentRequest(sensitiveRequestHeadersToRemove),
                getDocumentResponse(sensitiveResponseHeadersToRemove),
                snippets);
    }

    // Request(요청)
    private static OperationRequestPreprocessor getDocumentRequest() {
        return preprocessRequest(
                prettyPrint()
        );
    }

    // Request에 Header 존재할 경우 (헤더 삭제)
    private static OperationRequestPreprocessor getDocumentRequest(String... headersToRemove) {
        if (headersToRemove == null || headersToRemove.length == 0) {
            return getDocumentRequest();
        }
        return preprocessRequest(
                removeHeaders(headersToRemove),
                prettyPrint()
        );
    }

    // Response(응답)
    private static OperationResponsePreprocessor getDocumentResponse() {
        return preprocessResponse(
                prettyPrint()
        );
    }

    private static OperationResponsePreprocessor getDocumentResponse(String... headersToRemove) {
        if (headersToRemove == null || headersToRemove.length == 0) {
            return getDocumentResponse();
        }
        return preprocessResponse(
                removeHeaders(headersToRemove),
                prettyPrint()
        );
    }
}
