package com.bali.backend.repository;

import com.bali.backend.model.Post;
import com.bali.backend.model.User;
import com.bali.backend.model.VillageGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // =====================================
    // GLOBAL FEED
    // Only active non-deleted posts
    // =====================================
    Page<Post> findAllByDeletedFalseOrderByCreatedAtDesc(
            Pageable pageable
    );

    // =====================================
    // GROUP FEED
    // =====================================
    Page<Post> findAllByGroupAndDeletedFalseOrderByCreatedAtDesc(
            VillageGroup group,
            Pageable pageable
    );

    // =====================================
    // USER PROFILE POSTS
    // =====================================
    Page<Post> findAllByAuthorAndDeletedFalseOrderByCreatedAtDesc(
            User author,
            Pageable pageable
    );

    // =====================================
    // REPLIES OF POST
    // =====================================
    Page<Post> findAllByParentPostAndDeletedFalseOrderByCreatedAtAsc(
            Post parentPost,
            Pageable pageable
    );

    // =====================================
    // SEARCH POSTS
    // =====================================
    Page<Post> findByContentContainingIgnoreCaseAndDeletedFalseOrderByCreatedAtDesc(
            String keyword,
            Pageable pageable
    );

    // =====================================
    // COUNT USER POSTS
    // =====================================
    long countByAuthorAndDeletedFalse(
            User author
    );

    // =====================================
    // EXISTENCE CHECK
    // =====================================
    boolean existsByIdAndDeletedFalse(
            Long id
    );
}