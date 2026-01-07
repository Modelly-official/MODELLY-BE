package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.repository.FcmTokenRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;


    @Transactional(readOnly = true)
    public String getTokenByUserId(Long userId) {
        return fcmTokenRepository.findByUserId(userId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_FCM_TOKEN));
    }
}
