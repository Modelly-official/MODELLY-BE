package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;

    public void checkModel(User user){
        if (user.getUserRole() != UserRole.MODEL){
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }

    public Model getModelByUser(User user){
        return modelRepository.findByUser(user)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_MODEL));
    }

    public Model getModelByUserId(Long userId) {
        return modelRepository.findByUser_Id(userId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_MODEL));
    }
}
