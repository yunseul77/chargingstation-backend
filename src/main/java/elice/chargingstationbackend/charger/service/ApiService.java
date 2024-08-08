package elice.chargingstationbackend.charger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.chargingstationbackend.charger.dto.ChargerApiResponseDTO;
import elice.chargingstationbackend.charger.entity.Charger;
import elice.chargingstationbackend.charger.repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiService {
    private final ChargerRepository chargerRepository;

    private final String serviceKey = "4TbGmO/+UtIIPXlcDHVVZebt8muT2hdH8BixzgNuBePTYXaH3vbpY1PXhL7rZ1n7VrIR44UCyHU9DSMZLmTcAQ==";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JdbcTemplate jdbcTemplate;

    public String fetchChargerData(int pageNo, int numOfRows) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/EvCharger/getChargerInfo");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String response = sb.toString();

        // XML을 JSON으로 변환
        response = convertXmlToJsonIfNecessary(response);

        return response;
    }

    private String convertXmlToJsonIfNecessary(String response) {
        if (response.startsWith("<")) {
            try {
                JSONObject jsonObject = XML.toJSONObject(response);
                return jsonObject.toString();
            } catch (Exception e) {
                e.printStackTrace();
                // 변환 실패 시 원본 XML 반환
                return response;
            }
        }
        return response;
    }

    public void processAndSaveChargers() {
        int pageNo = 1;
        int numOfRows = 5000; // 페이지당 데이터 수
        int batchSize = 5000; // 배치 사이즈
        int maxPages = 500; // 최대 페이지 수 (무한 루프 방지)

        while (pageNo <= maxPages) {
            try {
                String jsonResponse = fetchChargerData(pageNo, numOfRows);
                ChargerApiResponseDTO dto = objectMapper.readValue(jsonResponse, ChargerApiResponseDTO.class);
                ChargerApiResponseDTO.Items items = dto.getItems();

                // 데이터가 비어있으면 종료
                if (items == null || items.getItemList() == null || items.getItemList().isEmpty()) {
                    System.out.println("불러올 데이터가 더이상 없습니다.");
                    break;
                }

                List<ChargerApiResponseDTO.Item> itemList = items.getItemList();
                List<Charger> chargers = itemList.stream()
                        .map(ChargerApiResponseDTO.Item::toEntity)
                        .collect(Collectors.toList());

                for (int i = 0; i < chargers.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, chargers.size());
                    List<Charger> batch = chargers.subList(i, end);
                    chargerRepository.saveAll(batch);
                    chargerRepository.flush();
                }

                pageNo++;

            } catch (IOException e) {
                // 실패 시 로깅 또는 알림 처리
                System.err.println("데이터를 불러오는 도중 에러발생: " + e.getMessage());
                e.printStackTrace();
                // 실패할 경우에는 계속 진행하거나 종료 결정
                break;
            }
        }
    }

//        @Scheduled(fixedRate = 600000) // 10분마다 API 호출
//        public void scheduledFetchAndSaveChargers() {
//            processAndSaveChargers();
//        }
}

