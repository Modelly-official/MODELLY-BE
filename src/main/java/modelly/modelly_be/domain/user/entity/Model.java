package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Model {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long id;

    @Column(name = "nickname", length = 20, nullable = false)                                     // VARCHAR(20)
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
}
