package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.domain.like.entity.DesignerLike;
import modelly.modelly_be.domain.like.repository.designerLikeRepository.DesignerLikeRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerLikeService {
    private final DesignerLikeRepository designerLikeRepository;

    public boolean existsByModelAndDesigner(Model model, Designer designer) {
        return designerLikeRepository.existsByModelAndDesigner(model, designer);
    }

    public void deleteByModelAndDesigner(Model model, Designer designer) {
        designerLikeRepository.deleteByModelAndDesigner(model, designer);
    }

    public void save(DesignerLike designerLike) {
        designerLikeRepository.save(designerLike);
    }

    public List<LikeDesignerListResponseDto> getLikeDesignerList(User user, Category category, Long cursorId, int size) {
        return designerLikeRepository.findAllByConditions(user.getId(),category, cursorId, size);
    }
}
