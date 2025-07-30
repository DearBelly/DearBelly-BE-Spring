package com.hanium.mom4u.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.crawling.dto.CrawlingResultDto;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SeleniumCrawler {

    private static final String MAIN_URL = "https://health.seoulmc.or.kr/healthCareInfo/pregnancyInfo.do";
    private static final String POST_URL = "https://health.seoulmc.or.kr/healthCareInfo/pregnancyView.do?boardPid=";

    private static final String FILE_PATH = "E:/CrawlingData/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void crawl() {

        WebDriver driver = new ChromeDriver();
        driver.get(MAIN_URL);

        List<CrawlingResultDto> resultList = new ArrayList<>();

        try {

            while (true) {

                List<WebElement> rows = driver.findElements(By.cssSelector("#divBoardList > table > tbody > tr"));

                for (WebElement row : rows) {
                    // 각 tr 별로 정보 수집
                    try {
                        String postId = row.findElement(By.cssSelector("label")).getAttribute("textContent");
                        String title = row.findElement(By.cssSelector("td > a")).getText().trim();
                        String subTitle = row.findElement(By.className("sort")).getText();
                        String link = POST_URL + postId;
                        log.info("postId {} 저장 성공...", postId);
                        String postedAt = row.findElement(By.cssSelector("td:nth-child(4)")).getText();

                        String content = detailCrawl(link);
                        if (content != null || content == " ") {
                            log.info("본문 크롤링 성공");
                        } else {
                            log.info("본문 크롤링 실패");
                        }


                        CrawlingResultDto resultDto = CrawlingResultDto.builder()
                                .postId(Long.parseLong(postId))
                                .title(title)
                                .subTitle(subTitle)
                                .link(link)
                                .postedAt(postedAt)
                                .content(content)
                                .build();

                        resultList.add(resultDto);
                        saveAsJson(resultDto);

                    } catch (Exception e) {
                        log.error("크롤링 오류 발생: {} , {}", e, e.getMessage());
                    }
                }

                // 다음 페이지 버튼 찾기
                try {
                    int currentPage = Integer.parseInt(driver.findElement(By.cssSelector("#divListPaging > li.active > a"))
                            .getAttribute("data-page"));
                    int nextPage = currentPage + 1;

                    // 다음 페이지의 링크를 찾는다 (data-page=다음페이지)
                    WebElement nextPageButton = driver.findElement(
                            By.cssSelector(String.format("#divListPaging > li > a[data-page='%d']", nextPage))
                    );

                    // 다음 버튼이 비활성화된 경우 종료
                    if (nextPageButton.getAttribute("class").contains("disabled")) {
                        break;
                    }

                    nextPageButton.click();
                    Thread.sleep(1000);
                } catch (NoSuchElementException e) {
                    System.out.println("더 이상 다음 페이지가 없습니다.");
                    break;
                }
            }

        } catch (Exception e) {
            log.error("크롤링 오류 발생: {} , {}", e, e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static void saveAsJson(CrawlingResultDto data) {
        try {
            objectMapper.writeValue(new File(FILE_PATH + data.getPostId() + ".json"), data);
            log.info("{} 크롤링 저장 완료", data.getPostId());
        } catch (IOException e) {
            log.error("{} Json 저장 실패", data.getPostId());
        }
    }

    private static String detailCrawl(String link) {
        try {
            WebDriver driver = new ChromeDriver();
            try {
                driver.get(link);
                Thread.sleep(200); // 또는 WebDriverWait 권장
                log.info("본문 링크로 이동...");

                return driver.findElement(By.cssSelector("#boardContent")).getText();
            } catch (Exception e) {
                log.error("본문 얻기 실패: {}", e.getMessage());
            } finally {
                driver.quit();
            }
        } catch (Exception e) {
            log.error("드라이버 생성 실패: {}", e.getMessage());
        }
        return null;
    }
}
