package elice.chargingstationbackend.charger.dto;

import elice.chargingstationbackend.charger.entity.Charger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChargerListResponseDTO {
    private String statId;
    private String businessName;
    private String statNm;
    private String addr;
    private String chgerType;
    private String speed;
    private Double chargingFee;
    private int parkingFee;
    private String stat;

    public ChargerListResponseDTO(Charger charger) {
        this.statId = charger.getStatId();
        // this.businessName = charger.getbBusinessOwner().getBusinessName();
        this.statNm = charger.getStatNm();
        this.addr = charger.getAddr();
        this.chgerType = charger.getChgerType();
        this.speed = charger.getSpeed();
        this.chargingFee = charger.getChargingFee();
        this.stat = charger.getStat();
    }
}
