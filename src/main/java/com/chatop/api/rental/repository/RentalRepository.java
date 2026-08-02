package com.chatop.api.rental.repository;

import com.chatop.api.rental.entity.RentalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<RentalEntity, Long> {

    List<RentalEntity> findAllByOrderByCreatedAtDesc();
}
