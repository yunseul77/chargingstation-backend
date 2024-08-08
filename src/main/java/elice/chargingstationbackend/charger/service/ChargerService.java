package elice.chargingstationbackend.charger.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elice.chargingstationbackend.charger.dto.ChargeStationDTO;
import org.springframework.stereotype.Service;

import elice.chargingstationbackend.charger.dto.ChargerDetailResponseDTO;
import elice.chargingstationbackend.charger.entity.Charger;
import elice.chargingstationbackend.charger.exception.ChargerNotFoundException;
import elice.chargingstationbackend.charger.repository.ChargerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChargerService {
    private final ChargerRepository chargerRepository;

    // 주변 충전소 리스트 자동 조회
    // public Page<ChargerListResponseDTO> getNearbyChargerList(Double latitute, Double longitute) {
    //     chargerRepository.findAll();

    // }

    // 충전소 세부 조회
    public ChargerDetailResponseDTO getChagerDetail(Long chargerId) {
        Charger chargerDetail = chargerRepository.findById(chargerId)
                                        .orElseThrow(() -> new ChargerNotFoundException("해당 충전소를 찾을 수 없습니다. ID: " + chargerId));
        return new ChargerDetailResponseDTO(chargerDetail);
    }


//    public String getChargestationApi(int pageNo, int numOfRows) throws IOException {
//
//        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/EvCharger/getChargerInfo"); /*URL*/
//        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") +
//                "=PRwlo8kAO7ZbkXrfg8pQL5YE346RF557TYQe%2F%2FW%2Bu3voecdcTdhYtC5SRD%2BzeTlRrccS%2BMDzqE%2BjwwBsJqmyWw%3D%3D"); ; /*Service Key*/
//        urlBuilder.append("&").append(URLEncoder.encode("pageNo",
//                StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8));
//        urlBuilder.append("&").append(URLEncoder.encode("numOfRows",
//                StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8));
//
//
//        URL url = new URL(urlBuilder.toString());
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        conn.setRequestProperty("Content-type", "application/json");
//        conn.setRequestProperty("Accept", "application/json");
//
//        BufferedReader rd;
//        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
//            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        } else {
//            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//        }
//
//        StringBuilder sb = new StringBuilder();
//        String line;
//        while ((line = rd.readLine()) != null) {
//            sb.append(line);
//        }
//
//        rd.close();
//        conn.disconnect();
//        return sb.toString();
//    }

    public List<ChargeStationDTO> getChargeStationsWithinDistance(Double lat, Double lng, double distance) throws IOException {

        double latRange = distance / 111.0;
        double lngRange = distance / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latRange;
        double maxLat = lat + latRange;
        double minLng = lng - lngRange;
        double maxLng = lng + lngRange;


//        String response = getChargestationApi(1,9999);
//
//        if (response != null) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(response);
//            JsonNode itemsArray = rootNode.path("items");
//
//            if (itemsArray.isArray() && !itemsArray.isEmpty()) {
//                JsonNode itemsNode = itemsArray.get(0).path("item");
//                List<ChargeStationDTO> chargeStationList = new ArrayList<>();
//
//                if (itemsNode.isArray()) {
//                    for (JsonNode itemNode : itemsNode) {
//                        ChargeStationDTO chargeStationDTO = ChargeStationDTO.builder()
//                                .chargerName(itemNode.path("statNm").asText())
//                                .chgerType(itemNode.path("chgerType").asText())
//                                .address(itemNode.path("addr").asText())
//                                .latitude(itemNode.path("lat").asDouble())
//                                .longitude(itemNode.path("lng").asDouble())
//                                .useTime(itemNode.path("useTime").asText())
//                                .busiCall(itemNode.path("busiCall").asText())
//                                .stat(itemNode.path("stat").asInt())
//                                .parkingFee(itemNode.path("parkingFree").asText())
//                                .limitYn(itemNode.path("limitYn").asText())
//                                .build();
//
//                        chargerRepository.save(chargeStationDTO.DtoToEntity(chargeStationDTO));
//
//                        double calculatedDistance = 6371 * Math.acos(
//                                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(chargeStationDTO.getLatitude())) *
//                                        Math.cos(Math.toRadians(chargeStationDTO.getLongitude()) - Math.toRadians(lng)) +
//                                        Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(chargeStationDTO.getLatitude()))
//                        );
//                        chargeStationDTO.setDistance(calculatedDistance);
//
//                        if (calculatedDistance <= distance) {
//                            chargeStationList.add(chargeStationDTO);
//                        }
//                    }
//                }
//
//                System.out.println(chargeStationList);
//                return chargeStationList;
//            }
//        }

        // response가 null일 경우 DB에서 조회
        List<Charger> chargeStations = chargerRepository.findWithinApproximateRange(minLat, maxLat, minLng, maxLng);

        return chargeStations.stream()
                .map(station -> {
                    double calculatedDistance = 6371 * Math.acos(
                            Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(station.getLatitude())) *
                                    Math.cos(Math.toRadians(station.getLongitude()) - Math.toRadians(lng)) +
                                    Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(station.getLatitude()))
                    );
                    return new ChargeStationDTO(station.getChargerName(), station.getChgerType(), station.getAddress(),
                            station.getLatitude(), station.getLongitude(), station.getUseTime(), station.getBusiCall(), station.getStat(),
                            station.getParkingFee(), calculatedDistance, station.getLimitYn());
                })
                .filter(dto -> dto.getDistance() < distance)
                .collect(Collectors.toList());
    }
}
