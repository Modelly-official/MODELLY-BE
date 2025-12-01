package modelly.modelly_be.global.geocoding;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeoCodingService {
    @Value("${google.maps.api.key}")
    private String googleApiKey;

    public LatLng getLatLngRes(String address) {
        try{
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(googleApiKey)
                    .build();

            GeocodingResult[] results = GeocodingApi.geocode(context, address)
                    .region("kr")
                    .await();

            if ( results.length>0) {
                LatLng coordinate = results[0].geometry.location;
                return coordinate;
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
