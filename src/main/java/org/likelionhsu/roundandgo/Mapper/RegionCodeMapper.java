package org.likelionhsu.roundandgo.Mapper;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RegionCodeMapper {

    private final Map<String, Integer> areaCodeMap = Map.ofEntries(
            Map.entry("서울특별시", 1),
            Map.entry("인천광역시", 2),
            Map.entry("대전광역시", 3),
            Map.entry("대구광역시", 4),
            Map.entry("광주광역시", 5),
            Map.entry("부산광역시", 6),
            Map.entry("울산광역시", 7),
            Map.entry("세종특별자치시", 8),
            Map.entry("경기도", 31),
            Map.entry("강원특별자치도", 32),
            Map.entry("충청북도", 33),
            Map.entry("충청남도", 34),
            Map.entry("경상북도", 35),
            Map.entry("경상남도", 36),
            Map.entry("전라북도", 37),
            Map.entry("전라남도", 38),
            Map.entry("제주특별자치도", 39)
    );

    private final Map<String, Map<String, Integer>> sigunguCodeMap = Map.ofEntries(
            Map.entry("서울특별시", Map.ofEntries(
                    Map.entry("강남구", 1), Map.entry("강동구", 2), Map.entry("강북구", 3), Map.entry("강서구", 4),
                    Map.entry("관악구", 5), Map.entry("광진구", 6), Map.entry("구로구", 7), Map.entry("금천구", 8),
                    Map.entry("노원구", 9), Map.entry("도봉구", 10), Map.entry("동대문구", 11), Map.entry("동작구", 12),
                    Map.entry("마포구", 13), Map.entry("서대문구", 14), Map.entry("서초구", 15), Map.entry("성동구", 16),
                    Map.entry("성북구", 17), Map.entry("송파구", 18), Map.entry("양천구", 19), Map.entry("영등포구", 20),
                    Map.entry("용산구", 21), Map.entry("은평구", 22), Map.entry("종로구", 23), Map.entry("중구", 24),
                    Map.entry("중랑구", 25)
            )),
            Map.entry("인천광역시", Map.ofEntries(
                    Map.entry("강화군", 1), Map.entry("계양구", 2), Map.entry("미추홀구", 3), Map.entry("남동구", 4),
                    Map.entry("동구", 5), Map.entry("부평구", 6), Map.entry("서구", 7), Map.entry("연수구", 8),
                    Map.entry("옹진군", 9), Map.entry("중구", 10)
            )),
            Map.entry("대전광역시", Map.of(
                    "대덕구", 1, "동구", 2, "서구", 3, "유성구", 4, "중구", 5
            )),
            Map.entry("대구광역시", Map.of(
                    "남구", 1, "달서구", 2, "달성군", 3, "동구", 4, "북구", 5,
                    "서구", 6, "수성구", 7, "중구", 8, "군위군", 9
            )),
            Map.entry("광주광역시", Map.of(
                    "광산구", 1, "남구", 2, "동구", 3, "북구", 4, "서구", 5
            )),
            Map.entry("부산광역시", Map.ofEntries(
                    Map.entry("강서구", 1), Map.entry("금정구", 2), Map.entry("기장군", 3), Map.entry("남구", 4),
                    Map.entry("동구", 5), Map.entry("동래구", 6), Map.entry("부산진구", 7), Map.entry("북구", 8), Map.entry("사상구", 9),
                    Map.entry("사하구", 10), Map.entry("서구", 11), Map.entry("수영구", 12), Map.entry("연제구", 13),
                    Map.entry("영도구", 14), Map.entry("중구", 15), Map.entry("해운대구", 16)
            )),
            Map.entry("울산광역시", Map.of(
                    "중구", 1, "남구", 2, "동구", 3, "북구", 4, "울주군", 5
            )),
            Map.entry("세종특별자치시", Map.of(
                    "세종특별자치시", 1
            )),
            Map.entry("경기도", Map.ofEntries(
                    Map.entry("가평군", 1), Map.entry("고양시", 2), Map.entry("과천시", 3), Map.entry("광명시", 4),
                    Map.entry("광주시", 5), Map.entry("구리시", 6), Map.entry("군포시", 7), Map.entry("김포시", 8),
                    Map.entry("남양주시", 9), Map.entry("동두천시", 10), Map.entry("부천시", 11), Map.entry("성남시", 12),
                    Map.entry("수원시", 13), Map.entry("시흥시", 14), Map.entry("안산시", 15), Map.entry("안성시", 16),
                    Map.entry("안양시", 17), Map.entry("양주시", 18), Map.entry("양평군", 19), Map.entry("여주시", 20),
                    Map.entry("연천군", 21), Map.entry("오산시", 22), Map.entry("용인시", 23), Map.entry("의왕시", 24),
                    Map.entry("의정부시", 25), Map.entry("이천시", 26), Map.entry("파주시", 27), Map.entry("평택시", 28),
                    Map.entry("포천시", 29), Map.entry("하남시", 30), Map.entry("화성시", 31)
            )),
            Map.entry("강원특별자치도", Map.ofEntries(
                    Map.entry("강릉시", 1), Map.entry("고성군", 2), Map.entry("동해시", 3), Map.entry("삼척시", 4),
                    Map.entry("속초시", 5), Map.entry("양구군", 6), Map.entry("양양군", 7), Map.entry("영월군", 8),
                    Map.entry("원주시", 9), Map.entry("인제군", 10), Map.entry("정선군", 11), Map.entry("철원군", 12),
                    Map.entry("춘천시", 13), Map.entry("태백시", 14), Map.entry("평창군", 15), Map.entry("홍천군", 16),
                    Map.entry("화천군", 17), Map.entry("횡성군", 18)
            )),
            Map.entry("충청북도", Map.ofEntries(
                    Map.entry("괴산군", 1), Map.entry("단양군", 2), Map.entry("보은군", 3), Map.entry("영동군", 4),
                    Map.entry("옥천군", 5), Map.entry("음성군", 6), Map.entry("제천시", 7), Map.entry("진천군", 8),
                    Map.entry("청원군", 9), Map.entry("청주시", 10), Map.entry("충주시", 11), Map.entry("증평군", 12)
            )),
            Map.entry("충청남도", Map.ofEntries(
                    Map.entry("공주시", 1), Map.entry("금산군", 2), Map.entry("논산시", 3), Map.entry("당진시", 4),
                    Map.entry("보령시", 5), Map.entry("부여군", 6), Map.entry("서산시", 7), Map.entry("서천군", 8),
                    Map.entry("아산시", 9), Map.entry("예산군", 11), Map.entry("천안시", 12), Map.entry("청양군", 13),
                    Map.entry("태안군", 14), Map.entry("홍성군", 15), Map.entry("계룡시", 16)
            )),
            Map.entry("경상북도", Map.ofEntries(
                    Map.entry("경산시", 1), Map.entry("경주시", 2), Map.entry("고령군", 3), Map.entry("구미시", 4),
                    Map.entry("김천시", 6), Map.entry("문경시", 7), Map.entry("봉화군", 8), Map.entry("상주시", 9),
                    Map.entry("성주군", 10), Map.entry("안동시", 11), Map.entry("영덕군", 12), Map.entry("영양군", 13),
                    Map.entry("영주시", 14), Map.entry("영천시", 15), Map.entry("예천군", 16), Map.entry("울릉군", 17),
                    Map.entry("울진군", 18), Map.entry("의성군", 19), Map.entry("청도군", 20), Map.entry("청송군", 21),
                    Map.entry("칠곡군", 22), Map.entry("포항시", 23)
            )),
            Map.entry("경상남도", Map.ofEntries(
                    Map.entry("거제시", 1), Map.entry("거창군", 2), Map.entry("고성군", 3), Map.entry("김해시", 4),
                    Map.entry("남해군", 5), Map.entry("마산시", 6), Map.entry("밀양시", 7), Map.entry("사천시", 8),
                    Map.entry("산청군", 9), Map.entry("양산시", 10), Map.entry("의령군", 12), Map.entry("진주시", 13),
                    Map.entry("진해시", 14), Map.entry("창녕군", 15), Map.entry("창원시", 16), Map.entry("통영시", 17),
                    Map.entry("하동군", 18), Map.entry("함안군", 19), Map.entry("함양군", 20), Map.entry("합천군", 21)
            )),
            Map.entry("전라북도", Map.ofEntries(
                    Map.entry("고창군", 1), Map.entry("군산시", 2), Map.entry("김제시", 3), Map.entry("남원시", 4),
                    Map.entry("무주군", 5), Map.entry("부안군", 6), Map.entry("순창군", 7), Map.entry("완주군", 8),
                    Map.entry("익산시", 9), Map.entry("임실군", 10), Map.entry("장수군", 11), Map.entry("전주시", 12),
                    Map.entry("정읍시", 13), Map.entry("진안군", 14)
            )),
            Map.entry("전라남도", Map.ofEntries(
                    Map.entry("강진군", 1), Map.entry("고흥군", 2), Map.entry("곡성군", 3), Map.entry("광양시", 4),
                    Map.entry("구례군", 5), Map.entry("나주시", 6), Map.entry("담양군", 7), Map.entry("목포시", 8),
                    Map.entry("무안군", 9), Map.entry("보성군", 10), Map.entry("순천시", 11), Map.entry("신안군", 12),
                    Map.entry("여수시", 13), Map.entry("영광군", 16), Map.entry("영암군", 17), Map.entry("완도군", 18),
                    Map.entry("장성군", 19), Map.entry("장흥군", 20), Map.entry("진도군", 21), Map.entry("함평군", 22),
                    Map.entry("해남군", 23), Map.entry("화순군", 24)
            )),
            Map.entry("제주특별자치도", Map.of(
                    "남제주군", 1, "북제주군", 2, "서귀포시", 3, "제주시", 4
            ))
    );

    public int getAreaCode(String province) {
        return areaCodeMap.getOrDefault(province, 0);
    }

    public int getSigunguCode(String province, String city) {
        return sigunguCodeMap.getOrDefault(province, Map.of()).getOrDefault(city, 0);
    }
}

