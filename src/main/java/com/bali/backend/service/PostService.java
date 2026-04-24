package com.bali.backend.service;

import com.bali.backend.model.Post;
import com.bali.backend.model.User;
import com.bali.backend.model.VillageGroup;
import com.bali.backend.repository.PostRepository;
import com.bali.backend.repository.UserRepository;
import com.bali.backend.repository.VillageGroupRepository;
import graphql.GraphqlErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VillageGroupRepository villageGroupRepository;

    @Autowired
    private UserRepository userRepository;

    // =====================================
    // GLOBAL FEED
    // =====================================
    public List<Post> getFeed(int page, int size) {

        return postRepository
                .findAllByDeletedFalseOrderByCreatedAtDesc(
                        PageRequest.of(page, size)
                )
                .getContent();
    }

    // =====================================
    // GROUP FEED
    // =====================================
    public List<Post> getVillageFeed(
            VillageGroup group,
            int page,
            int size
    ) {

        return postRepository
                .findAllByGroupAndDeletedFalseOrderByCreatedAtDesc(
                        group,
                        PageRequest.of(page, size)
                )
                .getContent();
    }

    // =====================================
    // SINGLE POST
    // =====================================
    public Post getPost(Long id) {

        return postRepository.findById(id)
                .filter(post -> !post.isDeleted())
                .orElseThrow(() ->
                        GraphqlErrorException.newErrorException()
                                .message("Post not found")
                                .build());
    }

    // =====================================
    // CREATE POST
    // =====================================
    public Post createPost(
            Post post,
            Long groupId,
            Long parentPostId,
            Long sharedPostId
    ) {

        // Group Post
        if (groupId != null) {

            VillageGroup group =
                    villageGroupRepository.findById(groupId)
                            .orElseThrow(() ->
                                    GraphqlErrorException.newErrorException()
                                            .message("Group not found")
                                            .build());

            post.setGroup(group);
        }

        // Reply Post
        if (parentPostId != null) {

            Post parent = getPost(parentPostId);

            post.setParentPost(parent);

            parent.setCommentsCount(parent.getCommentsCount() + 1);

            postRepository.save(parent);
        }

        // Shared Post
        if (sharedPostId != null) {

            Post shared = getPost(sharedPostId);

            post.setSharedPost(shared);

            shared.setSharesCount(shared.getSharesCount() + 1);

            postRepository.save(shared);
        }

        return postRepository.save(post);
    }

    // =====================================
    // UPDATE POST
    // =====================================
    public Post updatePost(
            Long postId,
            Long userId,
            String content
    ) {

        Post post = getPost(postId);

        validateOwner(post, userId);

        post.setContent(content);
        post.setEdited(true);

        return postRepository.save(post);
    }

    // =====================================
    // DELETE POST
    // =====================================
    public void deletePost(
            Long postId,
            Long userId
    ) {

        Post post = getPost(postId);

        validateOwner(post, userId);

        post.setDeleted(true);
        post.setDeletedAt(LocalDateTime.now());

        postRepository.save(post);
    }

    // =====================================
    // LIKE / UNLIKE
    // TEMP VERSION (counter only)
    // Later create PostLike table
    // =====================================
    public Boolean toggleLike(
            Long postId,
            Long userId
    ) {

        Post post = getPost(postId);

        post.setLikesCount(post.getLikesCount() + 1);

        postRepository.save(post);

        return true;
    }

    // =====================================
    // SHARE POST
    // =====================================
    public Post sharePost(
            Long postId,
            Long userId
    ) {

        Post original = getPost(postId);

        User user = userRepository.findById(userId)
                .orElseThrow();

        original.setSharesCount(original.getSharesCount() + 1);

        postRepository.save(original);

        Post shared = Post.builder()
                .author(user)
                .content(original.getContent())
                .postType(original.getPostType())
                .visibility(original.getVisibility())
                .sharedPost(original)
                .build();

        return postRepository.save(shared);
    }

    // =====================================
    // BOOKMARK
    // TEMP MOCK
    // Later create bookmark table
    // =====================================
    public Boolean toggleBookmark(
            Long postId,
            Long userId
    ) {
        getPost(postId);
        return true;
    }

    // =====================================
    // OWNER VALIDATION
    // =====================================
    private void validateOwner(
            Post post,
            Long userId
    ) {

        if (!post.getAuthor().getId().equals(userId)) {

            throw GraphqlErrorException.newErrorException()
                    .message("Unauthorized action")
                    .build();
        }
    }
}