package elice.chargingstationbackend.business.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import elice.chargingstationbackend.business.entity.ApprovalRequest;
import elice.chargingstationbackend.business.entity.BusinessOwner;
import elice.chargingstationbackend.business.exception.BusinessOwnerNotFoundException;
import elice.chargingstationbackend.business.exception.RequestNotFoundException;
import elice.chargingstationbackend.business.repository.ApprovalRequestRepository;
import elice.chargingstationbackend.business.repository.BusinessOwnerRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final EmailService emailService;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final String bucketName = "chargingstation"; // 생성한 버킷 이름

    public ApprovalRequest createApprovalRequest(
        Long ownerId, MultipartFile businessCertificate, MultipartFile identityProof) throws IOException {

        BusinessOwner businessOwner = businessOwnerRepository.findById(ownerId)
            .orElseThrow(() -> new BusinessOwnerNotFoundException(ownerId));

        String businessCertificatePath = uploadFileToGCS(businessCertificate);
        String identityProofPath = uploadFileToGCS(identityProof);

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.updateApprovalRequest(
            businessOwner, "Pending", LocalDateTime.now(),
            businessCertificatePath, identityProofPath
        );

        return approvalRequestRepository.save(approvalRequest);
    }

    private String uploadFileToGCS(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        BlobInfo blobInfo = storage.create(
            BlobInfo.newBuilder(bucketName, fileName).build(),
            file.getInputStream()
        );
        return blobInfo.getMediaLink();
    }

    public List<ApprovalRequest> getApprovalRequestsByOwnerId(Long ownerId) {
        BusinessOwner businessOwner = businessOwnerRepository.findById(ownerId)
            .orElseThrow(() -> new BusinessOwnerNotFoundException(ownerId));

        return approvalRequestRepository.findByBusinessOwner(businessOwner);
    }

    public ApprovalRequest updateApprovalRequest(Long requestId, ApprovalRequest updatedRequest) {
        ApprovalRequest existingRequest = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));

        existingRequest.updateApprovalRequest(
            updatedRequest.getBusinessOwner(), updatedRequest.getStatus(),
            updatedRequest.getTimestamp(), updatedRequest.getBusinessCertificatePath(), updatedRequest.getIdentityProofPath()
        );

        return approvalRequestRepository.save(existingRequest);
    }

    public ApprovalRequest approveRequest(Long requestId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));

        request.setStatus("Approved");
        ApprovalRequest approvedRequest = approvalRequestRepository.save(request);

        sendApprovalEmail(approvedRequest);

        return approvedRequest;
    }

    public ApprovalRequest rejectRequest(Long requestId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));

        request.setStatus("Rejected");
        ApprovalRequest rejectedRequest = approvalRequestRepository.save(request);

        sendRejectionEmail(rejectedRequest);

        return rejectedRequest;
    }

    private void sendApprovalEmail(ApprovalRequest request) {
        String to = request.getBusinessOwner().getOwnerEmail();
        String subject = "승인 요청 승인";
        String text = "귀하의 승인 요청이 승인되었습니다.";
        emailService.sendSimpleMessage(to, subject, text);
    }

    private void sendRejectionEmail(ApprovalRequest request) {
        String to = request.getBusinessOwner().getOwnerEmail();
        String subject = "승인 요청 거부";
        String text = "귀하의 승인 요청이 거부되었습니다.";
        emailService.sendSimpleMessage(to, subject, text);
    }
}
