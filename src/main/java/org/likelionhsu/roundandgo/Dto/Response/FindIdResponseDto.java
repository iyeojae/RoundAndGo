package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FindIdResponseDto {
    private List<String> foundIds; // 찾은 아이디들 (이메일들)
    private String message;
}
