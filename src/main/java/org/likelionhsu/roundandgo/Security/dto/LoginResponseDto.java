package org.likelionhsu.roundandgo.Security.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
}