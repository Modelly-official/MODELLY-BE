package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByDesignerAndModel(Designer designer, Model model);

    /* 채팅방 조회: 마지막 메세지 시간을 기준으로 정렬 + 페이징 */

    // 모델 ID로 조회
    @Query("""
        SELECT r FROM ChatRoom r
        LEFT JOIN Chatting c ON c.chatRoom = r
        WHERE r.model.id = :modelId
        GROUP BY r
        ORDER BY COALESCE(MAX(c.createdAt), r.createdAt) DESC
        """)
    Page<ChatRoom> findAllByModelIdOrderByLastMessageTimeDesc(
            @Param("modelId") Long modelId,
            Pageable pageable
    );

    // 디자이너 ID로 조회
    @Query("""
        SELECT r FROM ChatRoom r
        LEFT JOIN Chatting c ON c.chatRoom = r
        WHERE r.designer.id = :designerId
        GROUP BY r
        ORDER BY COALESCE(MAX(c.createdAt), r.createdAt) DESC
        """)
    Page<ChatRoom> findAllByDesignerIdOrderByLastMessageTimeDesc(
            @Param("designerId") Long designerId,
            Pageable pageable
    );
}
