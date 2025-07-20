package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Dto.CommunityRequestDto;
import org.likelionhsu.roundandgo.Dto.CommunityResponseDto;
import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.CommunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    public CommunityResponseDto createCommunity(User user, CommunityRequestDto request) {
        CommunityCategory category = CommunityCategory.fromLabel(request.getCategory());
        Community community = Community.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user.getNickname())
                .category(category)
                .user(user)
                .build();
        return new CommunityResponseDto(communityRepository.save(community));
    }

    public List<CommunityResponseDto> getAllPosts() {
        return communityRepository.findAll().stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public List<CommunityResponseDto> getPostsByCategory(String category) {
        CommunityCategory categoryEnum = CommunityCategory.fromLabel(category);
        return communityRepository.findByCategory(categoryEnum).stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public CommunityResponseDto getPost(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
        return new CommunityResponseDto(community);
    }

    public CommunityResponseDto updatePost(Long id, User user, CommunityRequestDto request) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        if (!community.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        community.setTitle(request.getTitle());
        community.setContent(request.getContent());
        community.setCategory(CommunityCategory.fromLabel(request.getCategory()));

        return new CommunityResponseDto(communityRepository.save(community));
    }

    public void deletePost(Long id, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
        if (!community.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }
        communityRepository.delete(community);
    }

    public List<CommunityResponseDto> getPostsByUser(User user) {
        return communityRepository.findByUser(user).stream()
                .map(CommunityResponseDto::new)
                .toList();
    }
}
