package modelly.modelly_be.global.security.repository;

import modelly.modelly_be.global.security.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
