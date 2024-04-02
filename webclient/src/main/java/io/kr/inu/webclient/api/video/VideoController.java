package io.kr.inu.webclient.api.video;

import io.kr.inu.core.video.service.VideoService;
import io.kr.inu.webclient.api.resolver.UserEmail;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "동영상 저장", description = "JWT를 헤더에 보내주세요. multipart로 보내주실때 key 값에 'video' 로 보내주세요")
    @PostMapping("/v1/upload/video")
    public String uploadImage(UserEmail email, @RequestPart(name = "video", required = false) MultipartFile video) throws IOException {
        return videoService.uploadVideo(video, email.getEmail());
    }
}
