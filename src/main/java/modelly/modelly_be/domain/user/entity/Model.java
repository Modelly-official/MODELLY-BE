package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Model extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long id;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)                                     // VARCHAR(20)
    private String nickname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public static Model of(User user, String nickname) {
        Model model = new Model();
        model.user = user;
        model.nickname = nickname;
        return model;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
