package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.CollectionLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionLocationRepository extends JpaRepository<CollectionLocation, Long> {

	/**
	 * 현 위치 기준 반경(미터) 이내 목적지 조회, 거리순 정렬.
	 * MySQL ST_Distance_Sphere 사용 (단위: 미터).
	 */
	@Query(value = """
		SELECT * FROM collection_locations
		WHERE ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lon, :lat)) <= :radiusMeters
		ORDER BY ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lon, :lat))
		""", nativeQuery = true)
	List<CollectionLocation> findWithinRadiusOrderByDistance(
			@Param("lat") double latitude,
			@Param("lon") double longitude,
			@Param("radiusMeters") int radiusMeters);
}
