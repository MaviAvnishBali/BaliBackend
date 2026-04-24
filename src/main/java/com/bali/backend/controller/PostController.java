package com.bali.backend.controller;

import com.bali.backend.model.Post;
import com.bali.backend.model.PostType;
import com.bali.backend.model.User;
import com.bali.backend.model.VillageGroup;
import com.bali.backend.model.VisibilityType;
import com.bali.backend.repository.UserRepository;
import com.bali.backend.service.PostService;
import graphql.GraphqlErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    // =====================================
    // FEED
    // =====================================
    @QueryMapping
    public List<Post> feed(
            @Argument int page,
            @Argument int size) {
        return postService.getFeed(page, size);
    }

    // =====================================
    // SINGLE POST
    // =====================================
    @QueryMapping
    public Post post(@Argument Long id) {
        return postService.getPost(id);
    }

    // =====================================
    // CREATE POST
    // =====================================
    @MutationMapping
    public Post createPost(

            @Argument String content,

            @Argument String imageUrl,

            @Argument Long villageId,

            @Argument String visibility, // PUBLIC / GROUP / PRIVATE

            @Argument String postType, // TEXT / IMAGE / VIDEO / POLL

            @Argument Long groupId,

            @Argument Long parentPostId, // reply

            @Argument Long sharedPostId // repost/share
    ) {

        User author = getAuthenticatedUser();

        // validation
        if ((content == null || content.trim().isEmpty())
                && parentPostId == null
                && sharedPostId == null) {

            throw GraphqlErrorException.newErrorException()
                    .message("Post content cannot be empty")
                    .build();
        }

        Post post = Post.builder()
                .author(author)
                .content(content == null ? "" : content.trim())
                .visibility(parseVisibility(visibility))
                .postType(parsePostType(postType))
                .likesCount(0)
                .commentsCount(0)
                .sharesCount(0)
                .repostsCount(0)
                .viewsCount(0)
                .edited(false)
                .deleted(false)
                .active(true)
                .build();

        return postService.createPost(
                post,
                groupId != null ? groupId : villageId,
                parentPostId,
                sharedPostId);
    }

    // =====================================
    // UPDATE POST
    // =====================================
    @MutationMapping
    public Post updatePost(
            @Argument Long postId,
            @Argument String content) {

        User user = getAuthenticatedUser();

        if (content == null || content.trim().isEmpty()) {
            throw GraphqlErrorException.newErrorException()
                    .message("Content cannot be empty")
                    .build();
        }

        return postService.updatePost(
                postId,
                user.getId(),
                content.trim());
    }

    // =====================================
    // DELETE POST
    // =====================================
    @MutationMapping
    public Boolean deletePost(@Argument Long postId) {

        User user = getAuthenticatedUser();

        postService.deletePost(postId, user.getId());

        return true;
    }

    // =====================================
    // LIKE / UNLIKE
    // =====================================
    @MutationMapping
    public Boolean toggleLike(@Argument Long postId) {

        User user = getAuthenticatedUser();

        return postService.toggleLike(postId, user.getId());
    }

    // =====================================
    // SHARE
    // =====================================
    @MutationMapping
    public Post sharePost(@Argument Long postId) {

        User user = getAuthenticatedUser();

        return postService.sharePost(postId, user.getId());
    }

    // =====================================
    // BOOKMARK
    // =====================================
    @MutationMapping
    public Boolean toggleBookmark(@Argument Long postId) {

        User user = getAuthenticatedUser();

        return postService.toggleBookmark(postId, user.getId());
    }

    // =====================================
    // HELPERS
    // =====================================
    private User getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {

            throw GraphqlErrorException.newErrorException()
                    .message("User not authenticated")
                    .build();
        }

        try {

            Object principal = authentication.getPrincipal();

            Long userId;

            if (principal instanceof Number number) {
                userId = number.longValue();

            } else if (principal instanceof String value) {
                userId = Long.parseLong(value);

            } else {
                throw new IllegalStateException("Unsupported principal type");
            }

            return userRepository.findById(userId)
                    .orElseThrow(() -> GraphqlErrorException.newErrorException()
                            .message("Authenticated user not found")
                            .build());

        } catch (Exception e) {

            throw GraphqlErrorException.newErrorException()
                    .message("User not authenticated")
                    .build();
        }
    }

    private VisibilityType parseVisibility(String value) {

        try {
            if (value == null)
                return VisibilityType.PUBLIC;

            return VisibilityType.valueOf(value.toUpperCase());

        } catch (Exception e) {

            throw GraphqlErrorException.newErrorException()
                    .message("Invalid visibility")
                    .build();
        }
    }

    private PostType parsePostType(String value) {

        try {
            if (value == null)
                return PostType.TEXT;

            return PostType.valueOf(value.toUpperCase());

        } catch (Exception e) {

            throw GraphqlErrorException.newErrorException()
                    .message("Invalid postType")
                    .build();
        }
    }

    // =====================================
    // SCHEMA MAPPINGS
    // =====================================
    @SchemaMapping(typeName = "Post", field = "villageGroup")
    public VillageGroup getVillageGroup(Post post) {
        return post.getGroup();
    }

    @SchemaMapping(typeName = "Post", field = "imageUrl")
    public String getImageUrl(Post post) {
        return null;
    }
}