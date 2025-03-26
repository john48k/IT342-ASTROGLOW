package com.astroglow.Repository;

import com.astroglow.Entity.OfflineLibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflineLibraryRepository extends JpaRepository<OfflineLibraryEntity, Integer> {

}
