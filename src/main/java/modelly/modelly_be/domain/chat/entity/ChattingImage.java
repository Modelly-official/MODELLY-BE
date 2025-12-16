package modelly.modelly_be.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Table(name = "chatting_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChattingImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_id", nullable = false)
    private Chatting chatting;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    private ChattingImage(Chatting chatting, String imageUrl) {
        this.chatting = chatting;
        this.imageUrl = imageUrl;
    }

    public static ChattingImage of(Chatting chatting, String imageUrl) {
        return new ChattingImage(chatting, imageUrl);
    }
}
