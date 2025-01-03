package com.cherrymango.foodiehub.controller.api;

import com.cherrymango.foodiehub.domain.Store;
import com.cherrymango.foodiehub.dto.StoreImageResponseDto;
import com.cherrymango.foodiehub.file.FileStore;
import com.cherrymango.foodiehub.repository.StoreRepository;
import com.cherrymango.foodiehub.service.StoreImageService;
import com.cherrymango.foodiehub.service.StoreService;
import com.cherrymango.foodiehub.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreImageApiController {
    private final FileStore fileStore;
    private final StoreRepository storeRepository;
    private final StoreImageService storeImageService;
    final private StoreService storeService;
    private final TokenUtil tokenUtil;

    @GetMapping("/image/{filename}")
    public Resource downloadImage(@PathVariable("filename") String filename) throws MalformedURLException {
        // 파일명이 왔을 때 UrlResource로 실제 파일에 접근해서 리소스를 가져온 다음 바이너리 데이터를 웹브라우저로 전송
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

//    @PostMapping("/images/{storeId}")
//    public ResponseEntity<Long> uploadImage(@PathVariable("storeId") Long storeId, @RequestParam("image") MultipartFile image) {
//        Long imageId = storeImageService.save(storeId, image);
//        return ResponseEntity.ok(imageId);
//    }

//    @GetMapping("/images/{storeId}")
//    public ResponseEntity<List<StoreImageResponseDto>> findAllImages(@PathVariable("storeId") Long storeId) {
//        Store store = storeRepository.findById(storeId).get();
//        List<StoreImageResponseDto> images = storeImageService.findImages(store).stream()
//                .map(image -> new StoreImageResponseDto(image.getId(), image.getStoreImageName()))
//                .toList();
//
//        return ResponseEntity.ok(images);
//    }

    // 이미지 삭제 (2025-01-03 박혜정)
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable("imageId") Long imageId) {
        storeImageService.delete(imageId);
        return ResponseEntity.ok().build();
    }


    // 이미지 로드 (2025-01-03 박혜정)
    @GetMapping("/images")
    public ResponseEntity<List<StoreImageResponseDto>> findAllImages(Principal principal, HttpServletRequest request) {
        Long userId = tokenUtil.getSiteUserIdOrThrow(principal, request);
        // userId로 storeId 찾기
        Long storeId = storeService.getStoreIdByUserId(userId);

        Store store = storeRepository.findById(storeId).get();
        List<StoreImageResponseDto> images = storeImageService.findImages(store).stream()
                .map(image -> new StoreImageResponseDto(image.getId(), image.getStoreImageName()))
                .toList();

        return ResponseEntity.ok(images);
    }

    // 이미지 등록 (2025-01-03 박혜정)
    @PostMapping("/images")
    public ResponseEntity<Long> uploadImage(Principal principal, HttpServletRequest request, @RequestParam("image") MultipartFile image) {
        Long userId = tokenUtil.getSiteUserIdOrThrow(principal, request);
        // userId로 storeId 찾기
        Long storeId = storeService.getStoreIdByUserId(userId);

        Long imageId = storeImageService.save(storeId, image);
        return ResponseEntity.ok(imageId);
    }


}
