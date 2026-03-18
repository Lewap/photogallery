package org.lewap.photogallery.repository;

import org.lewap.photogallery.model.PhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, String> {
    List<PhotoEntity> findByIsMissingFalseOrIsMissingNull();
}