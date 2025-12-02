package modelly.modelly_be.global.geocoding;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeoCodingService {
    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private GeoApiContext context;

    @PostConstruct
    public void init() {
        this.context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();
        }

    @PreDestroy
    public void cleanup() {
        if (context != null) {
            context.shutdown();
        }
    }

    public LatLng getLatLngRes(String address) {
        try{
            GeocodingResult[] results = GeocodingApi.geocode(context, address)
                    .region("kr")
                    .await();

            if (results != null && results.length>0) {
                LatLng coordinate = results[0].geometry.location;
                return coordinate;
            } else {
                throw new GeneralException(ErrorStatus.GEOCODING_FAILED);
            }

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.GEOCODING_FAILED);
        }
    }
}
