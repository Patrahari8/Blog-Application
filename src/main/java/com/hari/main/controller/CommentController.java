package com.hari.main.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hari.main.model.Comment;
import com.hari.main.model.Post;
import com.hari.main.model.User;
import com.hari.main.service.CommentService;
import com.hari.main.service.PostService;

@Controller
@RequestMapping("/comment")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private PostService postService;

	@PostMapping("/{id}")
	public String addComment(@PathVariable int id, Model model, @RequestParam String newComment,
			@RequestParam(value = "userName") String name, @RequestParam("userEmail") String userEmail) {
		commentService.saveComment(id, name, userEmail, newComment);
		return "redirect:/post/" + id;
	}

	@GetMapping("/{pid}/delete/{cid}")
	public String deleteComment(@PathVariable int pid, @PathVariable int cid) {

		Comment comment = commentService.getCommentById(cid);
		User user = commentService.getAuthenticatedUser();

		Optional<Post> post = postService.fetchPostById(pid);
		if (post.isPresent()) {
			if ((comment != null && commentService.isAuthorized(user, post.get()))) {
				commentService.deleteCommentById(cid);
			}
		}
		return "redirect:/post/" + pid;
	}

	@GetMapping("/{pid}/edit/{cid}")
	public String editComment(@PathVariable int pid, @PathVariable int cid, Model model) {

		Comment comment = commentService.getCommentById(cid);
		User user = commentService.getAuthenticatedUser();

		Optional<Post> post = postService.fetchPostById(pid);

		if (post.isPresent()) {
			if ((comment != null && commentService.isAuthorized(user, post.get()))) {
				model.addAttribute("commentEditForm", comment);
				model.addAttribute("postId", pid);

				model.addAttribute("userRole", user.getRole());

				return "editComment";
			}
		}
		return "redirect:/post/" + pid;
	}

	@PutMapping("/{id}/update")
	public String updateComment(@PathVariable int id, @RequestParam int postId,
			@ModelAttribute("commentEditForm") Comment comment) {

		Comment existingComment = commentService.getCommentById(id);

		User user = commentService.getAuthenticatedUser();
		Optional<Post> post = postService.fetchPostById(postId);

		if (post.isPresent()) {
			if ((existingComment != null && commentService.isAuthorized(user, post.get()))) {
				commentService.updateCommentById(id, postId, comment);
			}
		}
		return "redirect:/post/" + postId;
	}

}
