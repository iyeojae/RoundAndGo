package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationRequestDto;
import org.likelionhsu.roundandgo.Dto.CourseRecommendationResponseDto;
import org.likelionhsu.roundandgo.Service.CourseRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/recommendation")
public class CourseRecommendationController {

    private final CourseRecommendationService recommendationService;

//    @PostMapping
//    public ResponseEntity<CourseRecommendationResponseDto> createCourse(@RequestBody CourseRecommendationRequestDto request) {
//        return ResponseEntity.ok(recommendationService.createRecommendation(request));
//    }

    @PostMapping
    public ResponseEntity<CourseRecommendationResponseDto> createCourse(@RequestParam Long golfCourseId,
                                                                        @RequestParam String teeOffTime,
                                                                        @RequestParam String courseType) {
        return ResponseEntity.ok(recommendationService.createRecommendation(golfCourseId, teeOffTime, courseType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseRecommendationResponseDto> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getRecommendation(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseRecommendationResponseDto> updateCourse(@PathVariable Long id, @RequestBody CourseRecommendationRequestDto updateRequest) {
        return ResponseEntity.ok(recommendationService.updateRecommendation(id, updateRequest));
    }
}