package io.kr.inu.infra.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtDto {

    private final String accessToken;
    private final String refreshToken;

    @Builder
    public JwtDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
