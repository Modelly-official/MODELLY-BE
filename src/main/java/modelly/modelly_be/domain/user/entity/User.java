package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "login_id", length = 20, nullable = false, unique = true)   // VARCHAR(20)
    private String loginId;

    @Column(name = "password", length = 255, nullable = false)                   // VARCHAR(50)
    private String password;

    @Column(name = "email", length = 50, nullable = false, unique = true)                         // VARCHAR(50)
    private String email;

    @Column(name = "name", length = 20, nullable = false)                       // VARCHAR(20)
    private String name;

    @Column(name = "phone_num", length = 20, nullable = false)                                    // VARCHAR(20)
    private String phoneNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)// ENUM
    private Gender gender;

    @Column(name = "birth", nullable = false)                                                     // DATE
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)                      // ENUM (model/designer)
    private Role role;

    @Column(name = "nickname", length = 20, nullable = false)                                     // VARCHAR(20)
    private String nickname;

    @Column(name = "image_url", length = 255)                                   // VARCHAR(255)
    private String imageUrl;

    // 일단은 role 넣어둠.
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole; // USER or ADMIN

}
