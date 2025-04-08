package com.posts.post_platform.service.comment;

import com.posts.post_platform.exceptions.*;
import com.posts.post_platform.mapper.CommentMapper;
import com.posts.post_platform.model.Comment;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommentRepository;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.PostRepository;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.CommentRequest;
import com.posts.post_platform.response.CommentResponse;
import com.posts.post_platform.service.community.CommunityService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The `CommentServiceImpl` class implements the `CommentService` interface, providing various operations related to comments on posts within communities.
 * This service is responsible for adding comments, replying to comments, retrieving comments, and managing like/unlike actions on comments.
 *
 * It interacts with various repositories to perform CRUD operations on posts, users, comments, and communities.
 * Additionally, it uses the `CommentMapper` to transform data between the domain entities (e.g., `Comment`) and response DTOs (e.g., `CommentResponse`).
 * The class also interacts with the `CommunityService` to check user membership or roles within communities before allowing certain actions, especially in private communities.
 *
 * Dependencies for the service are injected via constructor injection, ensuring that the necessary repositories and services are available for use within the class.
 */
@Service
@EnableAsync
public class CommentServiceImpl implements CommentService{
    private final PostRepository postRepository;
    private final UserRepositories userRepository;
    private final CommunityRepository communityRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final CommunityService communityService;

    /**
     * Constructor to initialize the `CommentServiceImpl` with required dependencies.
     * @param postRepository The repository used to manage posts.
     * @param userRepository The repository used to manage user data.
     * @param communityRepository The repository used to manage community data.
     * @param commentMapper The mapper used to convert entities to DTOs.
     * @param commentRepository The repository used to manage comment data.
     * @param communityService The service used to handle community-related logic such as membership and roles.
     */
    public CommentServiceImpl(PostRepository postRepository, CommunityService communityService, UserRepositories userRepository, CommentMapper commentMapper, CommunityRepository communityRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.commentMapper = commentMapper;
        this.communityService = communityService;
        this.commentRepository = commentRepository;
    }

    /**
     * Adds a new comment to a post within a community.
     *
     * @param communityName the name of the community where the post belongs
     * @param postId the ID of the post to which the comment is being added
     * @param commentRequest the request object containing the details of the comment
     * @param username the username of the user who is adding the comment
     * @return the response object containing details of the newly added comment
     * @throws PostNotFoundException if the post with the specified ID does not exist
     * @throws UserNotFoundException if the user with the specified username does not exist
     * @throws CommunityNotFoundException if the community with the specified name does not exist
     * @throws IllegalArgumentException if the post does not belong to the specified community
     * @throws UnauthorizedActionException if the user is not authorized to add a comment in a private community
     */
    @Override
    @Transactional
    public CommentResponse addCommentToPost(String communityName, Long postId, CommentRequest commentRequest, String username) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        Community community =  communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        if (!post.getCommunity().getId().equals(community.getId())) throw new IllegalArgumentException("Post does not belong to the specified community.");
        if (community.getAccess_level().name().equalsIgnoreCase("private")) {
            boolean isCreator = communityService.isCreator(community.getId(), username);
            boolean isModerator = communityService.isModerator(community.getId(), username);
            boolean isMember = communityService.isMember(communityName, user.getId());
            if (isMember || isModerator || isCreator) {
                Comment comment = commentRepository.save(commentMapper.addCommentToPost(commentRequest, post, user));
                return commentMapper.convertCommentToResponse(comment);
            }
            throw new UnauthorizedActionException("User is neither creator nor moderator nor member in private community");
        }
        Comment comment = commentRepository.save(commentMapper.addCommentToPost(commentRequest, post, user));
        return commentMapper.convertCommentToResponse(comment);
    }

    /**
     * Replies to an existing comment on a post in a specific community.
     * It first validates the post, the comment to be replied to, and the user.
     * If the community is private, it checks if the user is authorized to reply (creator, moderator, or member).
     * If authorized, the reply is saved and returned.
     *
     * @param communityName the name of the community where the post exists
     * @param postId the ID of the post to which the comment belongs
     * @param commentId the ID of the comment being replied to
     * @param username the username of the user replying to the comment
     * @param commentRequest the reply details provided by the user
     * @return a CommentResponse containing the details of the reply
     * @throws PostNotFoundException if the post does not exist
     * @throws UserNotFoundException if the user does not exist
     * @throws CommunityNotFoundException if the community does not exist
     * @throws CommentNotFoundException if the comment does not exist
     * @throws IllegalArgumentException if the comment does not belong to the specified post
     * @throws UnauthorizedActionException if the user is not authorized to reply in a private community
     */
    @Override
    @Transactional
    public CommentResponse replyToComment(String communityName, Long postId, Long commentId, String username, CommentRequest commentRequest) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        Community community =  communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("Comment not found with comment id : " + commentId));
        if (!post.getCommunity().getId().equals(community.getId())) throw new IllegalArgumentException("Post does not belong to the specified community.");
        if (!comment.getPost().getId().equals(post.getId())) throw new IllegalArgumentException("Comment does not belong to the specified post.");
        if (community.getAccess_level().name().equalsIgnoreCase("private")) {
            boolean isCreator = communityService.isCreator(community.getId(), username);
            boolean isModerator = communityService.isModerator(community.getId(), username);
            boolean isMember = communityService.isMember(communityName, user.getId());
            if (isMember || isModerator || isCreator) {
                Comment newComment = commentRepository.save(commentMapper.replyToComment(commentRequest, post, user, comment));
                return commentMapper.convertCommentToResponse(newComment);
            }
            throw new UnauthorizedActionException("User is neither creator nor moderator nor member in private community");
        }
        Comment newComment = commentRepository.save(commentMapper.replyToComment(commentRequest, post, user, comment));
        return commentMapper.convertCommentToResponse(newComment);
    }

    /**
     * Retrieves a specific comment from a post within a community.
     * It checks the validity of the post, comment, and community.
     * If the community is private, it checks if the user is authorized to access the comment.
     * If authorized, the comment along with its child comments is returned.
     *
     * @param postId the ID of the post to which the comment belongs
     * @param communityName the name of the community where the post exists
     * @param commentId the ID of the comment to retrieve
     * @param username the username of the user requesting the comment
     * @return a CommentResponse containing the details of the comment and child comments
     * @throws PostNotFoundException if the post does not exist
     * @throws UserNotFoundException if the user does not exist
     * @throws CommunityNotFoundException if the community does not exist
     * @throws CommentNotFoundException if the comment does not exist
     * @throws IllegalArgumentException if the comment does not belong to the specified post
     * @throws UnauthorizedActionException if the user is not authorized to access the comment in a private community
     */
    @Override
    public CommentResponse getComment(Long postId, String communityName, Long commentId, String username) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        Community community =  communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("Comment not found with comment id : " + commentId));
        if (!post.getCommunity().getId().equals(community.getId())) throw new IllegalArgumentException("Post does not belong to the specified community.");
        if (!comment.getPost().getId().equals(post.getId())) throw new IllegalArgumentException("Comment does not belong to the specified post.");
        if (community.getAccess_level().name().equalsIgnoreCase("private")) {
            boolean isCreator = communityService.isCreator(community.getId(), username);
            boolean isModerator = communityService.isModerator(community.getId(), username);
            boolean isMember = communityService.isMember(communityName, user.getId());
            if (isMember || isModerator || isCreator) {
                return commentMapper.convertCommentToResponseWithChildComments(comment);
            }
            throw new UnauthorizedActionException("You can not access comment");
        }
        return commentMapper.convertCommentToResponseWithChildComments(comment);
    }

    /**
     * Retrieves all parent comments from a post in a specific community.
     * If the community is private, it checks if the user is authorized to view the comments.
     * If authorized, all parent comments are retrieved and returned.
     *
     * @param postId the ID of the post to which the comments belong
     * @param communityName the name of the community where the post exists
     * @param username the username of the user requesting the comments
     * @return a list of CommentResponse containing the details of all parent comments
     * @throws PostNotFoundException if the post does not exist
     * @throws CommunityNotFoundException if the community does not exist
     * @throws UnauthorizedActionException if the user is not authorized to access comments
     */
    @Override
    public List<CommentResponse> getAllCommentsFromPost(Long postId, String communityName, String username) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        Community community =  communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        if (!post.getCommunity().getId().equals(community.getId())) throw new IllegalArgumentException("Post does not belong to the specified community.");
        if (community.isPrivate()) {
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
            boolean isCreator = communityService.isCreator(community.getId(), username);
            boolean isModerator = communityService.isModerator(community.getId(), username);
            boolean isMember = communityService.isMember(communityName, user.getId());
            if (isMember || isModerator || isCreator) {
                List<Comment> parentComments = commentRepository.getAllParentCommentsFromPost(communityName, postId);
                return commentMapper.convertAllCommentsToResponse(parentComments);
            }
            throw new UnauthorizedActionException("You can not access comments");
        }
        List<Comment> parentComments = commentRepository.getAllParentCommentsFromPost(communityName, postId);
        return commentMapper.convertAllCommentsToResponse(parentComments);
    }

    /**
     * Retrieves all comments for a specified post, sorted by the provided sort option.
     * The sorting can be by "old" or "top" comments. Additionally, checks are made to ensure
     * the post belongs to the correct community and if the community is private, it checks
     * if the user has permission to view the comments.
     *
     * @param postId the ID of the post to retrieve comments for
     * @param communityName the name of the community the post belongs to
     * @param username the username of the person requesting the comments
     * @param sort the sorting method, either "old" or "top"
     * @return a list of CommentResponse objects containing the sorted comments
     * @throws PostNotFoundException if the post with the specified ID is not found
     * @throws CommunityNotFoundException if the community with the specified name is not found
     * @throws UnauthorizedActionException if the user is not authorized to view the comments
     * @throws IllegalArgumentException if the post does not belong to the specified community
     */
    @Override
    public List<CommentResponse> getAllCommentsBySorted(Long postId, String communityName, String username, String sort) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        Community community =  communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        if (!post.getCommunity().getId().equals(community.getId())) throw new IllegalArgumentException("Post does not belong to the specified community.");
        if (communityService.isCommunityPrivate(communityName)) {
            if (username == null) throw new UnauthorizedActionException("User is not authenticated for getting comments");
            User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
            boolean isCreator = communityService.isCreator(community.getId(), username);
            boolean isModerator = communityService.isModerator(community.getId(), username);
            boolean isMember = communityService.isMember(communityName, user.getId());
            if (isMember || isModerator || isCreator) {
                List<Comment> parentComments = getParentComments(communityName, postId, sort);
                return commentMapper.convertAllCommentsToResponse(parentComments);
            }
            throw new UnauthorizedActionException("You can not access comments");
        }
        List<Comment> parentComments = getParentComments(communityName, postId, sort);
        return commentMapper.convertAllCommentsToResponse(parentComments);
    }

    /**
     * Allows a user to like a specific comment on a post.
     * It increments the like count of the specified comment and saves the updated comment to the database.
     *
     * @param postId the ID of the post to which the comment belongs
     * @param commentId the ID of the comment to like
     * @param username the username of the user liking the comment
     * @return a message indicating the new like count of the comment
     * @throws UserNotFoundException if the user with the specified username is not found
     * @throws PostNotFoundException if the post with the specified ID is not found
     * @throws CommentNotFoundException if the comment with the specified ID is not found
     * @throws IllegalArgumentException if the comment does not belong to the specified post
     */
    @Override
    @Transactional
    public String likeComment(Long postId, Long commentId, String username) {
        userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        Comment comment = commentRepository.findCommentByIdWithLock(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with comment id " + commentId));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified post.");
        }
        int likeCount = (comment.getLike() == null ? 0 : comment.getLike());
        comment.setLike(likeCount + 1);
        commentRepository.save(comment);
        return "Comment new like count is " + comment.getLike();
    }

    /**
     * Allows a user to unlike a specific comment on a post.
     * It decrements the like count of the specified comment and saves the updated comment to the database.
     *
     * @param postId the ID of the post to which the comment belongs
     * @param commentId the ID of the comment to unlike
     * @param username the username of the user unliking the comment
     * @return a message indicating the new unlike count of the comment
     * @throws UserNotFoundException if the user with the specified username is not found
     * @throws PostNotFoundException if the post with the specified ID is not found
     * @throws CommentNotFoundException if the comment with the specified ID is not found
     * @throws IllegalArgumentException if the comment does not belong to the specified post
     */
    @Override
    @Transactional
    public String unlikeComment(Long postId, Long commentId, String username) {
        userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with post id : " + postId));
        Comment comment = commentRepository.findCommentByIdWithLock(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with comment id " + commentId));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified post.");
        }
        int unlikeCount = (comment.getUnlike() == null ? 0 : comment.getUnlike());
        comment.setUnlike(unlikeCount + 1);
        commentRepository.save(comment);
        return "Comment new unlike count is " + comment.getUnlike();
    }

    /**
     * Retrieves the parent comments for a given post based on the community name and the sort parameter.
     * The comments are fetched in the order specified by the sort parameter.
     *
     * @param communityName the name of the community the post belongs to
     * @param postId the ID of the post to retrieve comments for
     * @param sort the sorting method, either "old" or "top"
     * @return a list of parent comments for the specified post
     */
    private List<Comment> getParentComments(String communityName, Long postId, String sort) {
        List<Comment> parentComments;
        if (sort.equalsIgnoreCase("old")) {
            parentComments  = commentRepository.getAllParentCommentsFromPostSortedByOld(postId);
        } else if(sort.equalsIgnoreCase("top")) {
            parentComments = commentRepository.getAllParentCommentsFromPostSortedByTop(postId);
        } else {
            parentComments = commentRepository.getAllParentCommentsFromPost(communityName, postId);
        }
        return parentComments;
    }

}
