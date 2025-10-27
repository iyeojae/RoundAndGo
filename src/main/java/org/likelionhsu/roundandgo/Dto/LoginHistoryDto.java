package org.likelionhsu.roundandgo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginHistoryDto {
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
    private String loginType;
}

