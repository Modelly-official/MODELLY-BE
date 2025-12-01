package modelly.modelly_be.domain.like.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "찜 관련 API", description = "공고 / 디자이너 찜, 찜 취소, 찜한 리스트 조회")
public interface LikeSwagger {

    @Operation(summary = "공고 찜하기 / 찜 취소하기", description = "모델이 공고를 찜하거나 찜을 취소할 때 사용하는 API입니다.")
    ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId);

}
