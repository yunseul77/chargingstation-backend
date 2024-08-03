package elice.chargingstationbackend.charger.repository;

import java.util.Optional;
import java.util.List;

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

    // 충전소 검색(충전소 이름, 장소)
    @Query("SELECT c FROM Charger c WHERE c.chargerName LIKE %:searchTerm% OR c.address LIKE %:searchTerm%")
    List<Charger> searchByChargerNameOrAddress(@Param("searchTerm") String searchTerm);
}
