package io.kr.inu.core.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPubService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisPublisher redisPublisher;
    private final RedisSubscribeListener redisSubscribeListener;

    public void pubMsgChannel(String channel, LikeRequestDto message) {
        redisMessageListenerContainer.addMessageListener(redisSubscribeListener, new ChannelTopic(channel));
        redisPublisher.publish(new ChannelTopic(channel), message.toLikeDto());
    }

    public void cancelSubChannel(String channel) {
        redisMessageListenerContainer.removeMessageListener(redisSubscribeListener);
    }
}
