package com.example.mediwalk_be.domain.walk.repository;

import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionLocationRepository extends JpaRepository<CollectionLocation, Long> {

	@Query(value = """
		SELECT * FROM collection_locations
		WHERE ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lon, :lat)) <= :radiusMeters
		ORDER BY ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lon, :lat))
		""", nativeQuery = true)
	List<CollectionLocation> findWithinRadiusOrderByDistance(
			@Param("lat") double latitude,
			@Param("lon") double longitude,
			@Param("radiusMeters") int radiusMeters);

	List<CollectionLocation> findDistinctByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
			String name,
			String address,
			Pageable pageable);
}
