package modelly.modelly_be.domain.notification.repository.notificationRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.entity.QNotification;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.global.formatter.TimeFormatter;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<NotificationListResponse> findAllByUserAndType(User user, NotificationType notificationType, Long cursorId, int size) {
        QNotification qNotification = QNotification.notification;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(qNotification.user.id.eq(user.getId()));

        if (notificationType != null) {
            booleanBuilder.and(qNotification.notificationType.eq(notificationType));
        }

        if (cursorId != null) {
            booleanBuilder.and(qNotification.id.lt(cursorId));
        }

        List<Tuple> tuples = queryFactory
                .select(
                        qNotification.id,
                        qNotification.notificationType,
                        qNotification.content,
                        qNotification.createdAt,
                        qNotification.targetId,
                        qNotification.isRead
                )
                .from(qNotification)
                .where(booleanBuilder)
                .orderBy(qNotification.id.desc())
                .limit(size + 1)
                .fetch();

        UserRole userRole = user.getUserRole();

        return tuples.stream()
                .map(t -> {
                    NotificationType type = t.get(qNotification.notificationType);
                    String content = t.get(qNotification.content);

                    return new NotificationListResponse(
                        t.get(qNotification.id),
                            switch (type) {
                                case RESERVATION -> type.toDisplayReservationType(userRole, content);
                                case REVIEW -> type.toDisplayReviewType(userRole);
                                case CHATTING, SCHEDULE -> type.getDescription();
                            },
                        t.get(qNotification.content),
                        TimeFormatter.formatTimeAgo(t.get(qNotification.createdAt)),
                        t.get(qNotification.targetId),
                            t.get(qNotification.isRead)
                    );
                })
                .toList();
    }
}
