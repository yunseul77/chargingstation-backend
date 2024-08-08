package elice.chargingstationbackend.review.repository;

import elice.chargingstationbackend.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCharger_ChgerId(String chgerId);  // 여기를 chgerId로 변경
}
