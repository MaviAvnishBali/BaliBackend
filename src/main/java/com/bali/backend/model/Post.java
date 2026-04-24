package com.bali.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/*
========================================
CORE POST ENTITY (Production Ready)
========================================
Separate tables used for:
- likes
- comments
- media
- mentions
- hashtags
- bookmarks
- shares
========================================
*/

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_post_author", columnList = "author_id"),
                @Index(name = "idx_post_group", columnList = "group_id"),
                @Index(name = "idx_post_created", columnList = "createdAt"),
                @Index(name = "idx_post_parent", columnList = "parent_post_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Public ID for APIs
    @Column(unique = true, nullable = false)
    private String uuid;

    // -------------------------
    // Author
    // -------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // -------------------------
    // Group Post
    // -------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private VillageGroup group;

    // -------------------------
    // Main Content
    // -------------------------
    @Column(columnDefinition = "TEXT")
    private String content;

    // TEXT / IMAGE / VIDEO / POLL / REPOST
    @Enumerated(EnumType.STRING)
    private PostType postType;

    // PUBLIC / GROUP / PRIVATE
    @Enumerated(EnumType.STRING)
    private VisibilityType visibility;

    // Reply support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private Post parentPost;

    // Shared/Reposted post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_post_id")
    private Post sharedPost;

    // -------------------------
    // Cached Counters
    // -------------------------
    @Builder.Default
    private int likesCount = 0;

    @Builder.Default
    private int commentsCount = 0;

    @Builder.Default
    private int sharesCount = 0;

    @Builder.Default
    private int repostsCount = 0;

    @Builder.Default
    private long viewsCount = 0;

    // -------------------------
    // Flags
    // -------------------------
    @Builder.Default
    private boolean edited = false;

    @Builder.Default
    private boolean pinned = false;

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private boolean active = true;

    // -------------------------
    // Audit
    // -------------------------
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (uuid == null) uuid = java.util.UUID.randomUUID().toString();

        if (postType == null) postType = PostType.TEXT;

        if (visibility == null) visibility = VisibilityType.PUBLIC;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

/*
========================================
ENUMS
========================================
*/

/*
========================================
POST MEDIA
========================================
*/

@Entity
@Table(name = "post_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String url;

    // IMAGE / VIDEO
    private String mediaType;

    private String thumbnailUrl;

    private int sortOrder;
}

/*
========================================
POST LIKE
========================================
*/

@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;
}

/*
========================================
POST BOOKMARK
========================================
*/

@Entity
@Table(name = "post_bookmarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PostBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;
}

/*
========================================
POST MENTION
========================================
*/

@Entity
@Table(name = "post_mentions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PostMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    @ManyToOne
    private User mentionedUser;
}

/*
========================================
POST HASHTAG
========================================
*/

@Entity
@Table(name = "post_hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    private String hashtag;
}