package elice.chargingstationbackend.charger.controller;

import java.util.List;

import elice.chargingstationbackend.charger.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import elice.chargingstationbackend.charger.service.ChargerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/charger")
public class ChargerController {
    
    private final ChargerService chargerService;
    
    // 주변 충전소 리스트 자동 조회
    @PostMapping("/list")
    public ResponseEntity<List<ChargerListResponseDTO>> getNearbyChargerList(@RequestBody LocationDTO location,
                                                                             @RequestParam(required = false) List<String> chgerType,
                                                                             @RequestParam(required = false) List<String> output,
                                                                             @RequestParam(required = false) Double chargingFee,
                                                                             @RequestParam(required = false) String parkingFree,
                                                                             @RequestParam(required = false) List<String> kind,
                                                                             @RequestParam(required = false) List<Long> ownerIds) {


        List<ChargerListResponseDTO> nearByChargerList = chargerService.getNearbyChargerList(location, chgerType, output, chargingFee, parkingFree, kind, ownerIds);

        return ResponseEntity.ok().body(nearByChargerList);
    }

    // 충전소 검색(충전소 이름, 주소)
    @GetMapping("/search")
    public ResponseEntity<List<ChargerListResponseDTO>> searchCharger(@RequestParam String searchTerm) {
        List<ChargerListResponseDTO> resultList = chargerService.searchCharger(searchTerm);
        
        return ResponseEntity.ok().body(resultList);
    }
    
    // 충전소 세부 조회
    @GetMapping("/place/{statId}")
    public ResponseEntity<ChargerDetailResponseDTO> getChagerDetail(@PathVariable String statId) {
        ChargerDetailResponseDTO chargerDetail = chargerService.getChagerDetail(statId);
        
        return ResponseEntity.ok().body(chargerDetail);
    }

    // 사업자별 충전소 조회(토큰생성부분 미완성으로 ownerId를 쿠키로 토큰을 받아오지 못해서 임시로)
    @GetMapping("/ownerList")
    // @PreAuthorize("hasAuthority('')")
    public ResponseEntity<List<ChargerListResponseDTO>> getOwnerChargerList(@RequestParam Long ownerId) {
        // if(!jwtUtil.validateToken(accessToken)) {
        //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 요청입니다.");
        // }

        //Long ownerId = jwtUtil.getMemberIdFromToken(accessToken);

        List<ChargerListResponseDTO> ownerChargerList = chargerService.getOwnerChargerList(ownerId);
        return ResponseEntity.ok().body(ownerChargerList);
    }

    // 충전소 추가
    @PostMapping("/place/addCharger")
    // @PreAuthorize("hasAuthority('')")
    public ResponseEntity<String> addCharger(@RequestBody ChargerRequestDTO chargerRequestDTO) {
        // if(!jwtUtil.validateToken(accessToken)) {
        //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 요청입니다.");
        // }
        
        // Long ownerId = jwtUtil.getMemberIdFromToken(accessToken);

        String statId = chargerService.addCharger(chargerRequestDTO);

        return ResponseEntity.ok("충전소가 성공적으로 추가되었습니다. 충전소 식별번호 : " + statId);
    }

    // 충전소 수정
    @PatchMapping("/place/updateCharger/{statId}")
    // @PreAuthorize("hasAuthority('')")
    public ResponseEntity<String> updateCharger(@PathVariable String statId, @RequestBody ChargerRequestDTO chargerRequestDTO) {
        // if(!jwtUtil.validateToken(accessToken)) {
        //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 요청입니다.");
        // }
        
        // Long ownerId = jwtUtil.getMemberIdFromToken(accessToken);
        
        chargerService.updateCharger(statId, chargerRequestDTO);

        return ResponseEntity.ok("충전소 정보가 성공적으로 수정되었습니다. 충전소 식별번호 : " + statId);
    }

    // 충전소 삭제
    @DeleteMapping("/place/deleteCharger/{statId}")
    // @PreAuthorize("hasAuthority('')")
    public ResponseEntity<String> deleteCharger(@PathVariable String statId) {
        // if(!jwtUtil.validateToken(accessToken)) {
        //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 요청입니다.");
        // }
        
        // Long ownerId = jwtUtil.getMemberIdFromToken(accessToken);

        chargerService.deleteCharger(statId);

        return ResponseEntity.ok("충전소 정보가 성공적으로 삭제되었습니다. 충전소 식별번호 : " + statId);
    }
}
