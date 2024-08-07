package elice.chargingstationbackend.business.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BusinessOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownerId;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String ownerName = "Default Name"; // 기본 값 설정

    @NotBlank
    @Email
    @Size(max = 50)
    @Column(length = 50, nullable = false, unique = true)
    private String ownerEmail = "default@example.com"; // 기본 값 설정

    @NotBlank
    @Size(max = 60)
    @Column(length = 60, nullable = false)
    private String ownerPassword = "defaultPassword"; // 기본 비밀번호

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String businessOperatorName = "Default Operator"; // 새로운 필드 추가

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String contactInfo = "000-0000-0000"; // 기본 값 설정

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String address = "Default Address"; // 기본 값 설정

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String businessName = "Default Business"; // businessName 필드 추가

    public void updateDetails(BusinessOwner businessOwnerDetails) {
        this.ownerName = businessOwnerDetails.getOwnerName();
        this.ownerEmail = businessOwnerDetails.getOwnerEmail();
        this.ownerPassword = businessOwnerDetails.getOwnerPassword();
        this.businessName = businessOwnerDetails.getBusinessName();
        this.businessOperatorName = businessOwnerDetails.getBusinessOperatorName();
        this.contactInfo = businessOwnerDetails.getContactInfo();
        this.address = businessOwnerDetails.getAddress();
    }
}
