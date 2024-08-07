package elice.chargingstationbackend.business.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import elice.chargingstationbackend.business.service.ChargeStationService;

@RestController
@RequiredArgsConstructor
public class DataController {

    private final ChargeStationService chargeStationService;

    @GetMapping("/api/data/fetch-and-save")
    public String fetchAndSaveData() {
        try {
            // 데이터 불러오기 및 저장
            chargeStationService.fetchAndSaveChargeStationData();

            return "Data fetched and saved successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while fetching and saving data: " + e.getMessage();
        }
    }
}
