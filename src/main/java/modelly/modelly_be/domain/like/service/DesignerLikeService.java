package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.entity.DesignerLike;
import modelly.modelly_be.domain.like.repository.designerLikeRepository.DesignerLikeRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.stereotype.Service;

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
}
