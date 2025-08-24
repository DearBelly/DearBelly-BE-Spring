package com.hanium.mom4u.global.crawling.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.crawling.dto.CrawlingResultDto;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class SeleniumPregnancyCrawler {

    private static final String[] urlList = {
            "https://www.easylaw.go.kr/CSP/CnpClsMain.laf?popMenu=ov&csmSeq=735&ccfNo=1&cciNo=1&cnpClsNo=1&search_put=",
            "https://www.easylaw.go.kr/CSP/CnpClsMain.laf?popMenu=ov&csmSeq=735&ccfNo=1&cciNo=1&cnpClsNo=2&search_put=",
            "https://www.easylaw.go.kr/CSP/CnpClsMain.laf?popMenu=ov&csmSeq=735&ccfNo=1&cciNo=1&cnpClsNo=3&search_put="
    };

    @Value("${external.filepath}")
    private static String ROOT_FILE_PATH;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void crawl() {

        for (String url : urlList) {
            WebDriver driver = new ChromeDriver();
            driver.get(url);

            List<CrawlingResultDto> resultList = new ArrayList<>();

            try {
                String postId = "";
                StringBuilder id = new StringBuilder();
                try {
                    URI uri = URI.create(url);
                    String query = Optional.ofNullable(uri.getRawQuery()).orElse("");
                    Map<String, String> params = new HashMap<>();
                    for (String kv : query.split("&")) {
                        if (kv.isBlank() || !kv.contains("=")) continue;
                        String[] p = kv.split("=", 2);
                        params.put(p[0], URLDecoder.decode(p[1], StandardCharsets.UTF_8));
                    }
                    String csmSeq = params.getOrDefault("csmSeq", "NA");
                    String cnpClsNo = params.getOrDefault("cnpClsNo", "NA");
                    id.append(csmSeq).append("_").append(cnpClsNo);
                } catch (Exception e) {
                    throw GeneralException.of(StatusCode.FAILURE_TEST);
                }

                String title = driver.findElement(By.id("pageTitle")).getText().trim();

                log.info("postId {} 저장 성공...", postId);

                List<WebElement> webElementList = driver.findElements(By.cssSelector(".onDivbox"));

                String content = "";
                StringBuilder sb = new StringBuilder();
                for (WebElement element : webElementList) {
                    sb.append(element.getText().trim());
                }

                content = sb.toString();

                CrawlingResultDto resultDto = CrawlingResultDto.builder()
                        .postId(postId)
                        .title(title)
                        .subTitle(title)
                        .link(url)
                        .content(content)
                        .build();

                resultList.add(resultDto);
                saveAsJson(resultDto);

            } catch (Exception e) {
                log.error("크롤링 오류 발생: {} , {}", e, e.getMessage());
            }
        }
    }

    private static void saveAsJson(CrawlingResultDto data) {
        try {
            objectMapper.writeValue(new File(ROOT_FILE_PATH + "/PREGNANCY_PLANNING/" + data.getPostId() + ".json"), data);
            log.info("{} 크롤링 저장 완료", data.getPostId());
        } catch (IOException e) {
            log.error("{} Json 저장 실패", data.getPostId());
        }
    }
}
