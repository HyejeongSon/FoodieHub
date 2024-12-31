package com.cherrymango.foodiehub.service;


import com.cherrymango.foodiehub.domain.*;
import com.cherrymango.foodiehub.dto.*;
import com.cherrymango.foodiehub.file.FileStore;
import com.cherrymango.foodiehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
// @Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final SiteUserRepository siteUserRepository;
    private final TagRepository tagRepository;
    private final FileStore fileStore;
    private final StoreLikeRepository storeLikeRepository;
    private final StoreFavoriteRepository storeFavoriteRepository;

    public Long register(Long userId, AddStoreRequestDto addStoreRequestDto) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Store store = createStore(
                user,
                addStoreRequestDto.getName(),
                addStoreRequestDto.getIntro(),
                addStoreRequestDto.getPhone(),
                addStoreRequestDto.getAddress(),
                addStoreRequestDto.getCategory(),
                addStoreRequestDto.getParking(),
                addStoreRequestDto.getOperationHours(),
                addStoreRequestDto.getLastOrder(),
                addStoreRequestDto.getContent(),
                addStoreRequestDto.getMenus(),
                addStoreRequestDto.getTags(),
                addStoreRequestDto.getImages()
        );

        storeRepository.save(store);

        return store.getId();
    }

    public Store createStore(SiteUser user, String name, String intro, String phone, String address, Category category,
                             Integer parking, String operationHours, String lastOrder, String content,
                             List<AddMenuRequestDto> menus, List<String> tags, List<MultipartFile> images) {
        Store store = new Store();
        store.setUser(user);
        store.setName(name);
        store.setIntro(intro);
        store.setPhone(phone);
        store.setAddress(address);
        store.setCategory(category);
        store.setParking(parking);
        store.setOperationHours(operationHours);
        store.setLastOrder(lastOrder);
        store.setContent(content);
        store.setRegisterDate(LocalDateTime.now());

        if (menus != null) {
            for (AddMenuRequestDto menu : menus) {
                if (menu.getName() == null || menu.getPrice() == null) {
                    continue;
                }
                store.addMenu(Menu.createMenu(menu.getName(), menu.getPrice()));
            }
        }
        if (tags != null) {
            for (String tagName : tags) {
                Optional<Tag> tag = tagRepository.findByName(tagName);
                store.addStoreTag(StoreTag.createStoreTag(tag.get()));
            }
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }
            UploadImageDto uploadImageDto = fileStore.storeFile(image);
            store.addStoreImage(StoreImage.createStoreImage(uploadImageDto.getUploadFileName(), uploadImageDto.getStoreFileName()));
        }

        return store;
    }

    public UpdateStoreDetailDto getUpdateDetails(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + id));

        // Store 정보를 UpdateStoreDetailDto로 변환
        UpdateStoreDetailDto dto = new UpdateStoreDetailDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setIntro(store.getIntro());
        dto.setPhone(store.getPhone());
        dto.setAddress(store.getAddress());
        dto.setCategory(store.getCategory());
        dto.setParking(store.getParking());
        dto.setOperationHours(store.getOperationHours());
        dto.setLastOrder(store.getLastOrder());
        dto.setContent(store.getContent());
        dto.setRegisterDate(store.getRegisterDate());

        // Store의 태그 리스트를 DTO로 변환
        List<String> tags = store.getStoreTags().stream()
                .map(storeTag -> storeTag.getTag().getName())
                .toList();
        dto.setTags(tags);

        return dto;
    }

    @Transactional
    public void update(Long id, UpdateStoreRequestDto updateStoreRequestDto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + id));
        store.setName(updateStoreRequestDto.getName());
        store.setIntro(updateStoreRequestDto.getIntro());
        store.setPhone(updateStoreRequestDto.getPhone());
        store.setAddress(updateStoreRequestDto.getAddress());
        store.setCategory(updateStoreRequestDto.getCategory());
        store.setParking(updateStoreRequestDto.getParking());
        store.setOperationHours(updateStoreRequestDto.getOperationHours());
        store.setLastOrder(updateStoreRequestDto.getLastOrder());
        store.setContent(updateStoreRequestDto.getContent());
        updateTags(store, updateStoreRequestDto.getTags());
    }

    private void updateTags(Store store, List<String> tags) {
        // 기존 태그 이름 리스트
        Set<String> existingTagNames = store.getStoreTags().stream()
                .map(storeTag -> storeTag.getTag().getName())
                .collect(Collectors.toSet());

        // 추가할 태그 식별
        List<Tag> tagsToAdd = tags.stream()
                .filter(tagName -> !existingTagNames.contains(tagName)) // 기존에 없는 태그
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.createTag(tagName)))) // 없는 태그는 생성
                .toList();

        // 삭제할 태그 식별
        List<StoreTag> storeTagsToRemove = store.getStoreTags().stream()
                .filter(storeTag -> !tags.contains(storeTag.getTag().getName())) // 새 태그에 없는 기존 태그
                .toList();

        // StoreTag 추가
        for (Tag tag : tagsToAdd) {
            store.addStoreTag(StoreTag.createStoreTag(tag));
        }

        // StoreTag 삭제
        for (StoreTag storeTag : storeTagsToRemove) {
            store.getStoreTags().remove(storeTag);
        }
    }

    public StoreDetailResponseDto getStoreDetails(Long id, Long userId) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + id));

        SiteUser user = (userId != null)
                ? siteUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"))
                : null;

        Boolean isLiked = user != null && storeLikeRepository.existsByStoreAndUser(store, user);
        Boolean isFavorite = user != null && storeFavoriteRepository.existsByStoreAndUser(store, user);

        List<String> tags = extractTags(store.getStoreTags());
        List<String> imageNames = extractImageNames(store.getStoreImageList());
        Double avgRating = roundToFirstDecimal(calculateAverageRating(store));

        return mapToStoreDetailResponseDto(store, tags, imageNames, avgRating, isLiked, isFavorite);
    }

    private List<String> extractTags(List<StoreTag> storeTags) {
        return storeTags.stream()
                .map(storeTag -> storeTag.getTag().getName())
                .toList();
    }

    private List<String> extractImageNames(List<StoreImage> storeImages) {
        return storeImages.stream()
                .map(StoreImage::getStoreImageName)
                .toList();
    }

    private Double calculateAverageRating(Store store) {
        return store.getReviews().stream()
                .mapToDouble(Review::getAvgRating)
                .average()
                .orElse(0.0);
    }

    private Double roundToFirstDecimal(Double value) {
        return value != null ? Math.round(value * 10) / 10.0 : null;
    }

    private StoreDetailResponseDto mapToStoreDetailResponseDto(Store store, List<String> tags, List<String> imageNames,
                                                               Double avgRating, Boolean isLiked, Boolean isFavorite) {
        return StoreDetailResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .intro(store.getIntro())
                .phone(store.getPhone())
                .address(store.getAddress())
                .category(store.getCategory())
                .parking(store.getParking())
                .operationHours(store.getOperationHours())
                .lastOrder(store.getLastOrder())
                .content(store.getContent())
                .registerDate(store.getRegisterDate())
                .menus(store.getMenus().stream()
                        .map(menu -> new MenuResponseDto(null, menu.getName(), menu.getPrice()))
                        .toList())
                .images(imageNames)
                .tags(tags)
                .avgRating(avgRating)
                .likes(store.getStoreLikes().size())
                .isLiked(isLiked)
                .isFavorite(isFavorite)
                .build();
    }
}