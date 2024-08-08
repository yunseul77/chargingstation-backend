package elice.chargingstationbackend.charger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter@Setter
@Table(name="Charger")
@AllArgsConstructor
@NoArgsConstructor
public class Charger {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charger_id")
    private Long id;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "owner_id")
    // private Owner owner;

    @Column(name = "chargerName", columnDefinition="VARCHAR(255)")
    private String chargerName;

    @Column(name = "address", columnDefinition="VARCHAR(255)")
    private String address;

    @Column(name = "latitude", columnDefinition="DOUBLE")
    private Double latitude;

    @Column(name = "longitude", columnDefinition="DOUBLE")
    private Double longitude;

    @Column(name = "chgerType", columnDefinition="VARCHAR(10)")
    private String chgerType;

    @Column(nullable = true)
    private int slots;

    @Column(nullable = true)
    private int availableSlots;

    @Column(nullable = true)
    private double chargingSpeed;

    @Column
    private double chargingFee;

    @Column(name = "useTime", columnDefinition="VARCHAR(255)")
    private String useTime;

    @Column(name = "busiCall", columnDefinition="VARCHAR(20)")
    private String busiCall;

    @Column(name = "stat", columnDefinition="VARCHAR(10)")
    private Integer stat;

    @Column(name = "parkingFee", columnDefinition="VARCHAR(1)", nullable = false)
    private String parkingFee;

    @Column(name = "limitYn", columnDefinition="VARCHAR(1)", nullable = false)
    private String limitYn;

    @Builder
    public Charger(String chargerName, String address, Double latitude, Double longitude, String chgerType,
                   double chargingFee, String useTime, String busiCall, Integer stat, String parkingFee, String limitYn) {
        this.chargerName = chargerName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chgerType = chgerType;
        this.chargingFee = chargingFee;
        this.useTime = useTime;
        this.busiCall = busiCall;
        this.stat = stat;
        this.parkingFee = parkingFee;
        this.limitYn = limitYn;
    }

}
