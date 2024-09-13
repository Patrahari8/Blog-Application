package com.hari.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.hari.main.model.User;
import com.hari.main.service.CommentService;
import com.hari.main.service.UserService;

@Controller
@RequestMapping("/comment")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private UserService userService;

	@PostMapping("/{id}")
	public String addComment(@PathVariable int id, Model model, @RequestParam String newComment,
			@RequestParam(value="userName") String name, @RequestParam("userEmail") String userEmail ) {
		commentService.saveComment(id, name, userEmail, newComment);
		return "redirect:/post/" + id;
	}

	@GetMapping("/{pid}/delete/{cid}")
	public String deleteComment(@PathVariable int pid, @PathVariable int cid) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		Comment comment = commentService.getCommentById(cid);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		User user = userService.fetchByEmail(username);

		if ((comment != null && comment.getName().equals(user.getName())) || user.getRole().equals("ROLE_ADMIN")) {
			commentService.deleteCommentById(cid);
		}
		return "redirect:/post/" + pid;
	}

	@GetMapping("/{pid}/edit/{cid}")
	public String editComment(@PathVariable int pid, @PathVariable int cid, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		Comment comment = commentService.getCommentById(cid);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		User user = userService.fetchByEmail(username);

		if ((comment != null && comment.getName().equals(user.getName())) || user.getRole().equals("ROLE_ADMIN")) {
			model.addAttribute("commentEditForm", comment);
			model.addAttribute("postId", pid);
			
			model.addAttribute("userRole", user.getRole());

			
			return "editComment";
		}
		return "redirect:/post/" + pid;
	}

	@PutMapping("/{id}/update")
	public String updateComment(@PathVariable int id, @RequestParam int postId,
			@ModelAttribute("commentEditForm") Comment comment) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		Comment existingComment = commentService.getCommentById(id);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		User user = userService.fetchByEmail(username);
		if ((existingComment != null && existingComment.getName().equals(user.getName())) || user.getRole().equals("ROLE_ADMIN")) {
			commentService.updateCommentById(id, postId, comment);
		}
		return "redirect:/post/" + postId;
	}
}
