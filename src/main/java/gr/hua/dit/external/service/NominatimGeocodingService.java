package gr.hua.dit.external.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gr.hua.dit.external.config.NominatimProperties;
import gr.hua.dit.external.dto.AddressCoordinates; // <-- ΠΡΟΣΘΗΚΗ ΤΟΥ DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
public class NominatimGeocodingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NominatimGeocodingService.class);
    private final RestTemplate restTemplate;
    private final NominatimProperties nominatimProperties;

    public NominatimGeocodingService(RestTemplate restTemplate,
                                     NominatimProperties nominatimProperties) {
        if (restTemplate == null) throw new NullPointerException();
        if (nominatimProperties == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.nominatimProperties = nominatimProperties;
    }

    /**
     * Internal DTO to map the JSON response from Nominatim.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NominatimResult(String lat, String lon) {}

    /**
     * Retrieves the coordinates for a given address.
     */
    public Optional<AddressCoordinates> getCoordinates(String address) {
        try {
            // construct the API url
            URI url = UriComponentsBuilder.fromUriString(nominatimProperties.getUrl())
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUri();

            // set header
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", nominatimProperties.getUserAgent());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GET request
            ResponseEntity<NominatimResult[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResult[].class
            );

            // process response
            if (response.getBody() != null && response.getBody().length > 0) {
                LOGGER.debug("Received response from Nominatim: {}", (Object) response.getBody());
                NominatimResult firstResult = response.getBody()[0];

                double lat = Double.parseDouble(firstResult.lat());
                double lon = Double.parseDouble(firstResult.lon());

                return Optional.of(new AddressCoordinates(lat, lon));
            } else {
                LOGGER.warn("Nominatim returned no results for address: {}", address);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to geocode address: {}", address, e);
        }
        return Optional.empty();
    }
}