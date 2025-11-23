package modelly.modelly_be.global.security;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 사용자 정보 필요로 할 때 호출
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try {
            Long id = Long.valueOf(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
            return new AuthDetails(user);
        } catch (NumberFormatException e){
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);
        }
    }
}
