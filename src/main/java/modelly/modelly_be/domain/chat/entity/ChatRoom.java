package modelly.modelly_be.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Table(
    name = "chat_room",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"designer_id", "model_id"}) // 모델 - 디자이너 한 쌍당 채팅방 하나로 제약
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id", nullable = false)
    private Designer designer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    /* constructor */
    private ChatRoom(Designer designer, Model model) {
        this.designer = designer;
        this.model = model;
    }

    /* 정적 팩토리 메서드 */
    public static ChatRoom of(Designer designer, Model model) {
        return new ChatRoom(designer, model);
    }

    /* 특정 유저가 채팅방의 참여자인지 확인 */
    public boolean isParticipant(User user) {
        // UserId가 모델 id와 일치 or 디자이너 id와 일치하는지 확인
        return (this.model != null && this.model.getUser().getId().equals(user.getId()))
                || (this.designer != null && this.designer.getUser().getId().equals(user.getId()));
    }
}
