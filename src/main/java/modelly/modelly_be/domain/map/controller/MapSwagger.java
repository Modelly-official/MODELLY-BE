package modelly.modelly_be.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "지도 뷰 관련 API", description = "핀 리스트 API, 공고 리스트 API")
public interface MapSwagger {

    @Operation(summary = "샵(핀) 리스트 조회 API", description = """
            ### 모델이 지도에서 위치를 기반으로 샵 리스트를 조회하는 API입니다. \n
            ### Request Param
            `size` : 화면에서 보여질 샵의 수 \n
            `category` : HAIR, NAIL, TATTOO, EYELASH 중 택1 \n
            `userLatitude` : 사용자의 위도를 넣어주시면 됩니다. \n
            `userLongitude` : 사용자의 경도를 넣어주시면 됩니다. \n
            """)
    @GetMapping("/map/shops")
    ApiResponse<List<ShopResponse>> getShopList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Category category,
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude);
}
