package modelly.modelly_be.domain.infra.presignedURL.service;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.infra.presignedURL.dto.PresignedUrlListResponse;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.s3.PresignedUploadResponse;
import modelly.modelly_be.global.s3.S3Uploader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Uploader s3Uploader;
    private final DesignerService designerService;
    private final ModelService modelService;
    private final ReservationService reservationService;
    private final ChatRoomService chatRoomService;

    public PresignedUrlListResponse createRecruitmentImage(User user, int imageCount){
        designerService.checkDesigner(user);
        PresignedUrlListResponse response = s3Uploader.generatePresignedUrlList("recruitments", imageCount);

        return response;
    }

    public PresignedUrlListResponse createReviewsImage(User user, Long reservationId, int imageCount){
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);
        Reservation reservation = reservationService.getById(reservationId);

        if (!reservation.getModel().getId().equals(model.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        PresignedUrlListResponse response = s3Uploader.generatePresignedUrlList("reviews", imageCount);

        return response;
    }

    public PresignedUploadResponse createChattingImage(User user, Long roomId){
        chatRoomService.validateParticipation(user.getId(), roomId);

        PresignedUploadResponse response = s3Uploader.generatePresignedUrl("chat/" + roomId);

        return response;
    }

    public PresignedUploadResponse createProfileImage() {

        PresignedUploadResponse response = s3Uploader.generatePresignedUrl("profile");

        return response;
    }

    public PresignedUrlListResponse createPortfolioImage(User user, int imageCount) {
        designerService.checkDesigner(user);

        PresignedUrlListResponse response = s3Uploader.generatePresignedUrlList("portfolios", imageCount);
        return response;
    }
}
