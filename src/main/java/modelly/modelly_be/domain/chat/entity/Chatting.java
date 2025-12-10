package modelly.modelly_be.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Table(name = "chatting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatting extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_id")
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /* constructor */
    public Chatting(Long senderId, String message, Boolean isRead, MessageType messageType, ChatRoom chatRoom) {
        this.senderId = senderId;
        this.message = message;
        this.isRead = isRead;
        this.messageType = messageType;
        this.chatRoom = chatRoom;
    }

    /* 정적 팩토리 메서드 */
    public static Chatting of(Long senderId, String message, Boolean isRead, MessageType messageType, ChatRoom chatRoom) {
        return new Chatting(senderId, message, isRead, messageType, chatRoom);
    }
}
