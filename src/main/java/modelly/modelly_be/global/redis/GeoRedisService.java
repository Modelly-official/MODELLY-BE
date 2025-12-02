package modelly.modelly_be.global.redis;

import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeoRedisService {
    //아직 도입 X 추후 리팩토링 시 도입할 예정

    private static final String RECRUITMENT_GEO_KEY = "recruitment:geo";

    private final RedisTemplate<String, String> redisTemplate;
    private final GeoOperations<String, String> geoOps;

    public void addRecruitmentLocation(Long recruitmentId, LatLng location) {
        geoOps.add(RECRUITMENT_GEO_KEY, new Point(location.lng, location.lat), recruitmentId.toString());
    }

    public void removeRecruitmentLocation(Long recruitmentId) {
        geoOps.remove(RECRUITMENT_GEO_KEY, recruitmentId.toString());
    }

    public List<String> findNearByIds(double longitude, double latitude, double radius, int limit) {
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(limit);

        Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS));

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = geoOps
                .radius(RECRUITMENT_GEO_KEY, searchArea, args);

        return geoResults.getContent().stream()
                .map(loc -> loc.getContent().getName())
                .collect(Collectors.toList());

    }

}
