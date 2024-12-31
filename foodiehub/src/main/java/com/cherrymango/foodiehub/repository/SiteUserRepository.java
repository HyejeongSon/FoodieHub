package com.cherrymango.foodiehub.repository;
import com.cherrymango.foodiehub.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteUserRepository extends JpaRepository<SiteUser,Long> {
    Optional<SiteUser> findByEmail(String email); // email로 사용자 정보를 가져옴
    boolean existsByNickname(String nickname); // 닉네임 중복검사
}
