package gr.hua.dit.external.web.rest;

import gr.hua.dit.external.dto.AddressCoordinates;
import gr.hua.dit.external.dto.RouteMetrics;
import gr.hua.dit.external.dto.SmsRequest;
import gr.hua.dit.external.service.NominatimGeocodingService;
import gr.hua.dit.external.service.OpenRouteService;
import gr.hua.dit.external.service.RouteeSmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    private final RouteeSmsService smsService;
    private final NominatimGeocodingService geocodingService;
    private final OpenRouteService openRouteService;

    public ExternalApiController(RouteeSmsService smsService,
                                 NominatimGeocodingService geocodingService,
                                 OpenRouteService openRouteService) {

        if(smsService == null) throw new NullPointerException();
        if(geocodingService == null) throw new NullPointerException();
        if(openRouteService == null) throw new NullPointerException();

        this.smsService = smsService;
        this.geocodingService = geocodingService;
        this.openRouteService = openRouteService;
    }

    @PostMapping("/sms/send")
    public ResponseEntity<Void> sendSms(@RequestBody SmsRequest request) {
        System.out.println("External API: Received SMS request for " + request.to());
        smsService.send(request.to(), request.message());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/geo/search")
    public ResponseEntity<AddressCoordinates> geocode(@RequestParam String address) {
        System.out.println("External API: Geocoding request for " + address);
        return geocodingService.getCoordinates(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/route/calculate")
    public ResponseEntity<RouteMetrics> calculateRoute(
            @RequestParam double startLat, @RequestParam double startLon,
            @RequestParam double endLat, @RequestParam double endLon) {

        System.out.println("External API: Route calculation request");
        return openRouteService.getDistanceAndDuration(startLat, startLon, endLat, endLon)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
