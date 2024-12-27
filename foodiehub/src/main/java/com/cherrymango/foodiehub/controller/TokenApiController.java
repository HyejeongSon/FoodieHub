package com.cherrymango.foodiehub.controller;

import com.cherrymango.foodiehub.dto.CreateAccessTokenRequest;
import com.cherrymango.foodiehub.dto.CreateAccessTokenResponse;
import com.cherrymango.foodiehub.service.RefreshTokenService;
import com.cherrymango.foodiehub.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("http://localhost:3000")
public class TokenApiController {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    // 새로운 엑세스 토큰 발급
    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request){
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
    }

    // 리프레시 토큰 삭제 API
    @DeleteMapping("/api/refresh-token")
    public ResponseEntity deleteRefreshToken(){
        refreshTokenService.delete();
        return ResponseEntity.ok().build();
    }

}
