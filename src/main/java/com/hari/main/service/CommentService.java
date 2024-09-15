package com.hari.main.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hari.main.dao.CommentRepository;
import com.hari.main.dao.PostRepository;
import com.hari.main.model.Comment;
import com.hari.main.model.Post;
import com.hari.main.model.User;

@Service
public class CommentService {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserService userService;

	@Transactional
	public void saveComment(int id, String name, String email, String newComment) {
		Optional<Post> optionalPost = postRepository.findById(id);
		if (!optionalPost.isPresent()) {
			throw new IllegalArgumentException("Post not found with ID: " + id);
		}

		Post post = optionalPost.get();
		Comment comment = new Comment();

		String[] splitName = name.split(",");
		comment.setName(splitName.length == 2 ? splitName[1] : splitName[0]);

		String[] splitEmail = email.split(",");
		comment.setEmail(splitEmail.length == 2 ? splitEmail[1] : splitEmail[0]);

		comment.setComment(newComment);
		comment.setPost(post);
		comment.setCreated_at(LocalDate.now());
		comment.setUpdated_at(LocalDate.now());
		commentRepository.save(comment);
	}

	@Transactional
	public void deleteCommentById(int id) {
		commentRepository.deleteById(id);
	}

	@Transactional
	public void updateCommentById(int id, int postId, Comment comment) {
		Post post = postRepository.findById(postId).get();

		for (Comment com : post.getComments()) {
			if (com.getId() == id) {
				String c = comment.getComment();

				com.setComment(c);
				com.setUpdated_at(LocalDate.now());
				commentRepository.save(com);
			}
		}
		postRepository.save(post);
	}

	public Comment getCommentById(int cid) {
		return commentRepository.findById(cid).orElse(null);
	}

	public boolean isAuthorized(User user, Post post) {
		return post.getAuthor().getName().equals(user.getName()) || user.getRole().equals("ROLE_ADMIN");
	}

	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			return userService.fetchByEmail(userDetails.getUsername());
		}
		return null;
	}

}
