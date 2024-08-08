package elice.chargingstationbackend.charger.dto;

import elice.chargingstationbackend.charger.entity.Charger;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeStationDTO {
    private String chargerName;
    private String chgerType;
    private String address;
    private double latitude;
    private double longitude;
    private String useTime;
    private String busiCall;
    private Integer stat;
    private String parkingFee;
    private double distance;
    private String limitYn;

    public Charger DtoToEntity(ChargeStationDTO chargeStationDTO){
        return Charger.builder()
                .chargerName(chargeStationDTO.getChargerName())
                .chgerType(chargeStationDTO.getChgerType())
                .address(chargeStationDTO.getAddress())
                .latitude(chargeStationDTO.getLatitude())
                .longitude(chargeStationDTO.getLongitude())
                .useTime(chargeStationDTO.getUseTime())
                .busiCall(chargeStationDTO.getBusiCall())
                .stat(chargeStationDTO.getStat())
                .parkingFee(chargeStationDTO.getParkingFee())
                .limitYn(chargeStationDTO.getLimitYn())
                .build();
    }
}

