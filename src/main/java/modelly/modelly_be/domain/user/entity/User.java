package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.internal.SocialSignupBase;
import modelly.modelly_be.domain.notification.entity.NotificationSetting;
import modelly.modelly_be.domain.user.entity.enums.Gender;
import modelly.modelly_be.domain.user.entity.enums.LoginType;
import modelly.modelly_be.domain.user.entity.enums.Permission;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;


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

    @Column(name = "name", length = 20)
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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private NotificationSetting notificationSetting;

    // Designer, Model과 연관관계 설정
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Model model;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Designer designer;

    /* 비밀번호 변경 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateFromSocialSignup(SocialSignupBase base) {
        this.phoneNum = base.getPhoneNum();
        this.gender = base.getGender();
        this.birth = base.getBirth();
        this.imageUrl = base.getImageUrl();
        this.userRole = base.getUserRole() == UserRole.MODEL ? UserRole.MODEL : UserRole.DESIGNER_PENDING;
    }

    public void createNotificationSetting() {
        this.notificationSetting = NotificationSetting.builder()
                .user(this)
                .build();
    }

    public void updateNotificationSetting(NotificationSetting notificationSetting) {
        this.notificationSetting = notificationSetting;
    }

    public void updateImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void updateMyPage(
            Gender gender,
            LocalDate birth,
            String imageUrl
    ) {
        this.gender = gender;
        this.birth = birth;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

}
