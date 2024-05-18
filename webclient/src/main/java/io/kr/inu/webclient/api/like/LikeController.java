package io.kr.inu.webclient.api.like;

import io.kr.inu.core.like.dto.EachVideoLikes;
import io.kr.inu.core.like.dto.UpLikeReqDto;
import io.kr.inu.core.like.dto.VideoLikeWhether;
import io.kr.inu.core.like.service.LikeService;
import io.kr.inu.core.redis.LikeRequestDto;
import io.kr.inu.core.redis.RedisPubService;
import io.kr.inu.core.video.dto.FindVideoResponseDto;
import io.kr.inu.webclient.api.resolver.UserEmail;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;
    private final RedisPubService redisSubscribeService;

    @GetMapping("/v1/get/likes")
    @Operation(summary = "동영상 좋아요 개수 조회", description = "쿼리 파라미터로 영상 식별자를 보내주세요.")
    public ResponseEntity<EachVideoLikes> getVideoLikes(UserEmail userEmail, @RequestParam Long videoId) {
        return ResponseEntity.ok(likeService.getVideoLike(userEmail.getEmail(), videoId));
    }

    @PostMapping("/v1/insert/like")
    @Operation(summary = "동영상 좋아요 추가", description = "JWT를 헤더에 삽입해주세요. 영상 식별자를 Json 형식으로 보내주세요")
    public ResponseEntity<Void> insertVideoLike(@RequestParam String channel, @RequestBody LikeRequestDto message) {
        redisSubscribeService.pubMsgChannel(channel, message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v1/delete/like")
    @Operation(summary = "동영상 좋아요 삭제", description = "JWT를 헤더에 삽입해주세요. 영상 식별자를 Json 형식으로 보내주세요")
    public ResponseEntity<Void> deleteVideoLikes(UserEmail userEmail, @RequestBody UpLikeReqDto reqDto) {
        likeService.deleteLike(reqDto, userEmail.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/get/like/videos")
    @Operation(summary = "좋아요한 동영상 조회", description = "JWT를 헤더에 보내주세요. 조회하려는 페이지와 동영상 개수를 입력해주세요.")
    public ResponseEntity<FindVideoResponseDto> findVideoByUserLikes(UserEmail userEmail, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(likeService.findVideoByUserLikes(userEmail.getEmail(), page, size));
    }

    @GetMapping("/v1/get/like/video")
    @Operation(summary = "개별 동영상 좋아요 여부 확인", description = "JWT를 헤더에 보내주세요. 조회하려는 동영상의 식별자를 보내주세요.")
    public ResponseEntity<VideoLikeWhether> findVideoByUserLikes(UserEmail userEmail, @RequestParam Long videoId) {
        return ResponseEntity.ok(likeService.checkVideoLikeByUser(userEmail.getEmail(), videoId));
    }
}
