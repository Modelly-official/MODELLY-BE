package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.internal.SocialSignupBase;
import modelly.modelly_be.global.entity.BaseEntity;

import java.time.LocalDate;


@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", length = 20, unique = true)
    private String loginId;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "phone_num", length = 20)
    private String phoneNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    // 일단 permission 넣어둠.
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Permission permission; // USER or ADMIN

    /* 비밀번호 변경 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateFromSocialSignup(SocialSignupBase base) {
        this.phoneNum = base.getPhoneNum();
        this.gender = base.getGender();
        this.birth = base.getBirth();
        this.imageUrl = base.getImageUrl();
        this.userRole = base.getUserRole();
    }
}
