package com.vocabularyai.admin.repository;

import com.vocabularyai.admin.entity.TopicTagQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicTagQueueRepository extends JpaRepository<TopicTagQueue, Long> {

    Page<TopicTagQueue> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);

    Optional<TopicTagQueue> findByCefrWordIdAndStatus(String cefrWordId, String status);

    List<TopicTagQueue> findByCefrWordIdIn(List<String> cefrWordIds);

    boolean existsByCefrWordIdAndStatus(String cefrWordId, String status);
}
