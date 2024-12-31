package com.cherrymango.foodiehub.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name="siteuser")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity

public class SiteUser implements UserDetails { // UserDetails를 상속받아 인증 객체로 사용

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid",updatable = false)
    private Long id;

    @Column(name="email",nullable=false,unique=true)
    private String email;

    @Column(name="password",nullable = true)
    private String password;

    // 성명
    @Column(name="name")
    private String name;

    // 별명
    @Column(name="nickname", unique = true)
    private String nickname;

    // 권한
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    // 소셜
    @Column(name = "provider", nullable = false)
    private String provider; // "local", "google", "github" 등

    // 일반
    @Column(name="cellphone", nullable = true)
    private String cellphone;

    // 사업자 등록증 번호
    @Column(name="businessno", nullable=true, unique = true)
    private String businessno;

    // 프로필 사진
    @Column(name = "profileimageurl", nullable = true) // 프로필 이미지 경로는 필수 값이 아니므로 nullable=true
    private String profileImageUrl;



    @Builder
    public SiteUser(String email, String password,String nickname,
                    String name,String cellphone,String role,
                    String provider, String businessno) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.cellphone = cellphone;
        this.role = Role.fromString(role); // 안전한 변환
        this.provider = provider != null ? provider : "local";
        this.businessno =businessno;
    }

    // 사용자 이름 변경
    public SiteUser update(String nickname){
        this.nickname = nickname;
        return this;
    }

    @Override //권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DB에서 저장된 role 값을 권한으로 설정
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // 사용자의 id를 반환(고유한 값)
    @Override
    public String getUsername(){
        return email;
    }

    // 사용자의 패스워드 반환
    @Override
    public String getPassword() {
        return password;
    }

    // 계쩡 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // ture -> 만료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부 반환
        return true; // true -> 잠금 되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // ture -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }
}
