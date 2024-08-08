package elice.chargingstationbackend.charger.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import elice.chargingstationbackend.charger.entity.Charger;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
    
    // @Query("SELECT id, name, ST_AsText(location) AS location" +
    // "FROM locations" + 
    // "WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT(? - ?)')) < 5000;")
    // Page<Charger> findChargerByLocation(Pageable pageable);

    // 충전소 세부 조회
    Optional<Charger> findById(Long chargerId);
    
    //위도와 경도를 기반으로 특정 거리 이내의 충전소 조회
    @Query("SELECT ch FROM Charger ch WHERE ch.latitude BETWEEN :minLat AND :maxLat AND ch.longitude BETWEEN :minLng AND :maxLng")
    List<Charger> findWithinApproximateRange(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat, @Param("minLng") Double minLng, @Param("maxLng") Double maxLng);

}
