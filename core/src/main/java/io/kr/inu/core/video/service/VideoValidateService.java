package io.kr.inu.core.video.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoValidateService {

    private final RestTemplate restTemplate;

    public void validateHarmVideo(MultipartFile video) throws IOException {
        String apiUrl = "http://43.203.240.230:5000/process_video/sensitive_classify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        Resource videoResource = new InputStreamResource(video.getInputStream()) {
            @Override
            public long contentLength() {
                return video.getSize();
            }

            @Override
            public String getFilename() {
                return video.getOriginalFilename();
            }
        };

        formData.add("video", videoResource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        RestTemplate restTemplate = new RestTemplate();
        String responseData = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        if(responseData != null && responseData.contains("true")) {
            throw new IllegalArgumentException("유해 동영상입니다.");
        }

        log.info("응답 값 출력 : " + responseData);
    }
}