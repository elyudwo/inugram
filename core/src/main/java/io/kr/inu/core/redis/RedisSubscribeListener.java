package io.kr.inu.core.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kr.inu.core.like.domain.LikeEntity;
import io.kr.inu.core.like.repository.LikeRepository;
import io.kr.inu.core.user.domain.UserEntity;
import io.kr.inu.core.user.repository.UserRepository;
import io.kr.inu.core.video.domain.VideoEntity;
import io.kr.inu.core.video.repository.VideoRepository;
import io.kr.inu.infra.redis.LikeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscribeListener implements MessageListener {

    private final RedisTemplate<String, Object> template;
    private final ObjectMapper objectMapper;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = template.getStringSerializer().deserialize(message.getBody());
            LikeDto dto = objectMapper.readValue(publishMessage, LikeDto.class);

            log.info("Redis Subscribe Channel : " + dto.getUserId());
            log.info("Redis SUB Message : {}", publishMessage);

            UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow();
            VideoEntity video = videoRepository.findById(dto.getVideoId()).orElseThrow();
            LikeEntity like = LikeEntity.builder()
                            .user(user)
                            .video(video)
                            .build();
            likeRepository.save(like);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}