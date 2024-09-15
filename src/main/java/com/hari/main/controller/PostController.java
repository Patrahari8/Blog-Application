package com.hari.main.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hari.main.model.Post;
import com.hari.main.model.User;
import com.hari.main.service.PostService;
import com.hari.main.service.UserService;

@Controller
@RequestMapping("/post")
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private UserService userService;

	@GetMapping
	public String blogPage(Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size, @RequestParam(defaultValue = "publishedAt") String sortField,
			@RequestParam(defaultValue = "desc") String sortDirection) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

		Page<Post> paginatedPosts = postService.getPaginatedPosts(pageable);
		model.addAttribute("totalPages", paginatedPosts.getTotalPages());
		model.addAttribute("allPost", paginatedPosts.getContent());
		postService.addCommonAttributes(model, pageable);

		model.addAttribute("isAuthenticated", postService.isAuthenticated());

		return "index";
	}

	@GetMapping("/{id}")
	public String postPage(@PathVariable int id, Model model) {
		Optional<Post> post = postService.fetchPostById(id);
		if (!post.isPresent()) {
			return "redirect:/post";
		}

		User user = postService.getAuthenticatedUser();
		if (user != null) {
			model.addAttribute("userName", user.getName());
			model.addAttribute("userEmail", user.getEmail());
			model.addAttribute("userRole", user.getRole());
		} else {
			model.addAttribute("userName", null);
		}

		model.addAttribute("post", post.get());
		return "post";
	}

	@GetMapping("/create")
	public String createPage(Model model) {
		User user = postService.getAuthenticatedUser();
		if (user == null) {
			return "redirect:/login";
		}

		model.addAttribute("post", new Post());
		model.addAttribute("authorName", user.getName());
		model.addAttribute("userRole", user.getRole());

		return "create";
	}

	@PostMapping("/creatingprocess")
	public String addBlog(@ModelAttribute Post post, @RequestParam("tagsString") String tags,
			@RequestParam("authorName") String authorName) {

		User loggedInUser = postService.getAuthenticatedUser();
		if (loggedInUser == null) {
			return "redirect:/login";
		}

		User author;

		System.out.println("the author name is "+authorName);
		if (postService.isAdmin(loggedInUser)) {
			author = userService.fetchByName(authorName);
			if (author == null) {
				return "redirect:/post/create";
			}
		} else {
			author = loggedInUser;
		}
		postService.savePost(author, post, tags);
		return "redirect:/post";
	}

	@GetMapping("/{id}/edit")
	public String editPage(@PathVariable int id, Model model) {
		User user = postService.getAuthenticatedUser();
		Post post = postService.fetchPostById(id).orElse(null);

		if (post == null || (!post.getAuthor().getName().equals(user.getName()) && !postService.isAdmin(user))) {
			return "redirect:/post";
		}

		model.addAttribute("post", post);
		model.addAttribute("tag", postService.stringOfTags(post));
		model.addAttribute("isAuthorFieldDisabled", !postService.isAdmin(user));

		return "update";
	}

	@PutMapping("/{id}/update")
	public String updatePost(@RequestParam("authorName") String authorName, @PathVariable int id,
			@ModelAttribute Post post, @RequestParam("updateTags") String updateTags) {

		User user = postService.getAuthenticatedUser();
		Post existingPost = postService.fetchPostById(id).orElse(null);

		if (existingPost != null
				&& (existingPost.getAuthor().getEmail().equals(user.getEmail()) || postService.isAdmin(user))) {
			postService.updatePost(id, authorName, post, updateTags);
		}
		return "redirect:/post";
	}

	@GetMapping("/{id}/delete")
	public String deletePost(@PathVariable int id) {
		User user = postService.getAuthenticatedUser();
		Post post = postService.fetchPostById(id).orElse(null);

		if (post != null && (post.getAuthor().getEmail().equals(user.getEmail()) || postService.isAdmin(user))) {
			postService.deletePostById(id);
		}
		return "redirect:/post";
	}

	@GetMapping("/search")
	public String searchPosts(@RequestParam String query, @RequestParam(defaultValue = "asc") String sortOrder,
			Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {

		Pageable pageable = PageRequest.of(page, size,
				sortOrder.equalsIgnoreCase("asc") ? Sort.by("publishedAt").ascending()
						: Sort.by("publishedAt").descending());

		Page<Post> searchResults = postService.searchPosts(query, pageable);
		model.addAttribute("allPost", searchResults.getContent());
		model.addAttribute("totalPages", searchResults.getTotalPages());

		postService.addCommonAttributes(model, pageable);
		model.addAttribute("sortOrder", sortOrder);

		return "index";
	}

	@GetMapping("/filterBy")
	public String filter(@RequestParam(required = false) List<String> tagId,
			@RequestParam(required = false) String selectedDate, @RequestParam(required = false) List<String> author,
			@RequestParam(defaultValue = "asc") String sortOrder, Model model,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Post> filterResults = postService.filter(tagId, selectedDate, author, sortOrder, pageable);

		model.addAttribute("filteredPosts", filterResults.getContent());
		model.addAttribute("totalPages", filterResults.getTotalPages());
		model.addAttribute("selectedAuthors", author);
		model.addAttribute("selectedTags", tagId);
		model.addAttribute("sortOrder", sortOrder);

		postService.addCommonAttributes(model, pageable);

		return (tagId == null || tagId.isEmpty()) && (selectedDate == null || selectedDate.isEmpty())
				&& (author == null || author.isEmpty()) ? "redirect:/post" : "index";
	}

	@GetMapping("/sortByDate")
	public String sortByDate(@RequestParam(defaultValue = "asc") String sortOrder, Model model,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), "publishedAt"));
		Page<Post> paginatedPosts = postService.getPaginatedPosts(pageable);

		model.addAttribute("allPost", paginatedPosts.getContent());
		model.addAttribute("totalPages", paginatedPosts.getTotalPages());

		postService.addCommonAttributes(model, pageable);
		model.addAttribute("sortOrder", sortOrder);

		return "index";
	}

}
