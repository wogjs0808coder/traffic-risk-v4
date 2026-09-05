package com.trafficrisk.v4.route;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RouteQueryRepository extends JpaRepository<RouteQuery, Long> {

    Page<RouteQuery> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
