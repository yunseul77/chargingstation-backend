package elice.chargingstationbackend.charger.controller;

import elice.chargingstationbackend.charger.dto.ChargeStationDTO;
import elice.chargingstationbackend.charger.dto.CoordinateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import elice.chargingstationbackend.charger.dto.ChargerDetailResponseDTO;
import elice.chargingstationbackend.charger.dto.ChargerListResponseDTO;
import elice.chargingstationbackend.charger.service.ChargerService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
public class ChargerController {
    
    private final ChargerService chargerService;

    // 주변 충전소 리스트 자동 조회
    // @GetMapping
    // public ResponseEntity<Page<ChargerListResponseDTO>> getNearbyChargerList(@RequestBody Double latitude, @RequestBody Double longitute) {
    //     chargerService.getNearbyChargerList(latitude, longitute);
    // }

    // 충전소 세부 조회
    @GetMapping("/place/{chargerId}")
    public ResponseEntity<ChargerDetailResponseDTO> getChagerDetail(@PathVariable Long chargerId) {
        ChargerDetailResponseDTO chargerDetail = chargerService.getChagerDetail(chargerId);
        return ResponseEntity.ok().body(chargerDetail);
    }

    @PostMapping("/getChargerInfo")
    public ResponseEntity<List<ChargeStationDTO>> findChargestation(@RequestBody CoordinateDTO coordinateDTO) throws IOException {

        double lat = coordinateDTO.getLat();
        double lng = coordinateDTO.getLng();
        double distance = 10.0; //10km

        List<ChargeStationDTO> chargeStationDTOList =  chargerService.getChargeStationsWithinDistance(lat, lng, distance);

        return ResponseEntity.ok(chargeStationDTOList);
    }
}
