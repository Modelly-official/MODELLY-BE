package modelly.modelly_be.domain.chat.repository;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByDesignerAndModel(Designer designer, Model model);

    /* 모델 ID로 채팅방 조회 */
    @Query("""
        select cr
        from ChatRoom cr
        join fetch cr.designer d
        join fetch d.user du
        where cr.model.id = :modelId
        """)
    List<ChatRoom> findAllByModelIdWithDesigner(Long modelId);

    /* 디자이너 ID로 채팅방 조회 */
    @Query("""
        select cr
        from ChatRoom cr
        join fetch cr.model m
        join fetch m.user mu
        where cr.designer.id = :designerId
        """)
    List<ChatRoom> findAllByDesignerIdWithModel(Long designerId);
}
