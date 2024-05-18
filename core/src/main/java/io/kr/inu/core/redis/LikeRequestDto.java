package io.kr.inu.core.redis;

import io.kr.inu.infra.redis.LikeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequestDto {

    private Long userId;
    private Long videoId;

    public LikeDto toLikeDto() {
        return LikeDto.builder()
                .userId(userId)
                .videoId(videoId)
                .build();
    }
}
