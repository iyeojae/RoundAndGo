package org.likelionhsu.roundandgo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Common.ProfileColor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageResponseDto {
    private String url;
    private String nickname;
    private ProfileColor profileColor;
}
