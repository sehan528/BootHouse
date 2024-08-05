package likelion.eight.domain.user.model;

import jakarta.servlet.http.HttpServletResponse;
import likelion.eight.common.domain.exception.CertificationFailedException;
import likelion.eight.common.domain.exception.ResourceNotFoundException;
import likelion.eight.common.service.CookieService;
import likelion.eight.common.service.port.ClockHolder;
import likelion.eight.user.enums.RoleType;
import likelion.eight.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String name;
    private final String address;
    private final String email;
    private final String password;
    private final String phoneNumber;
    private final String certificationCode;
    private final UserStatus userStatus;
    private final RoleType roleType;
    private final Long lastLoginAt;
    private final byte[] image;

    @Builder
    public User(Long id, String name, String address, String email, String password, String phoneNumber, String certificationCode, UserStatus userStatus, RoleType roleType, Long lastLoginAt, byte[] image) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.certificationCode = certificationCode;
        this.userStatus = userStatus;
        this.roleType = roleType;
        this.lastLoginAt = lastLoginAt;
        this.image = image;
    }

    public User login(ClockHolder clockHolder, String password){
        if (!this.password.equals(password)) {
            throw new ResourceNotFoundException("비밀번호가 틀렸습니다.");
        }
        return User.builder()
                .id(id)
                .name(name)
                .address(address)
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .certificationCode(certificationCode)
                .userStatus(userStatus)
                .roleType(roleType)
                .lastLoginAt(clockHolder.millis())
                .image(image)
                .build();

    }

    public User certificate(String certificationCode){
        if (!this.certificationCode.equals(certificationCode)) {
            throw new CertificationFailedException("인증 코드 불일치");
        }
        return User.builder()
                .id(id)
                .name(name)
                .address(address)
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .certificationCode(certificationCode)
                .userStatus(UserStatus.ACTIVE)
                .roleType(roleType)
                .lastLoginAt(lastLoginAt)
                .image(image)
                .build();

    }
}
