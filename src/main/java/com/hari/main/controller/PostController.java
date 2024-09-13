package com.hari.main.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
			@RequestParam(defaultValue = "6") int size) {

		Page<Post> paginatedPosts = postService.getPaginatedPosts(page, size);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginatedPosts.getTotalPages());
		model.addAttribute("allPost", paginatedPosts.getContent());
		model.addAttribute("allTags", postService.fatchAllTagPosts());
		model.addAttribute("allPostForDate", postService.fetchAllPostsForDistinctDates());
		model.addAttribute("allAuthors", postService.fetchAllPostsForDistinctAuthor());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
				&& !(authentication.getPrincipal() instanceof String);

		model.addAttribute("isAuthenticated", isAuthenticated);

		return "index";
	}

	@GetMapping("/{id}")
	public String postPage(@PathVariable int id, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()
				&& authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();
			User user = userService.fetchByEmail(username);

			model.addAttribute("userName", user.getName());
			model.addAttribute("userEmail", username);
			model.addAttribute("userRole", user.getRole());

		} else {
			model.addAttribute("userName", null);

		}

		model.addAttribute("post", postService.fetchPostById(id).orElse(null));
		return "post";
	}

	@GetMapping("/create")
	public String createPage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();

		User user = userService.fetchByEmail(username);
		String displayName = user != null ? user.getName() : username;

		model.addAttribute("post", new Post());
		model.addAttribute("authorName", displayName);
		model.addAttribute("userRole", user.getRole());

		return "create";
	}

	@PostMapping("/creatingprocess")
	public String addingBlog(@ModelAttribute Post post, @RequestParam("tagsString") String tags,
	        @RequestParam("authorName") String authorName, Model model) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    String username = userDetails.getUsername();

	    User loggedInUser = userService.fetchByEmail(username);
	    User author;

	    if (loggedInUser.getRole().equals("ROLE_ADMIN")) {

	        author = userService.fetchByName(authorName.split(",")[1]);

	        if (author == null) {
	        	System.out.println("lets find name of uuthor" + author + "  " + authorName);
	            return "redirect:/post/create";
	        } else {
	            System.out.println("Author found: " + author.getName());
	        }
	    } else {
	        author = loggedInUser;
	    }

	    postService.savePost(author, post, tags);

	    return "redirect:/post";
	}


	@GetMapping("/{id}/edit")
	public String editPage(@PathVariable int id, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();
			User user = userService.fetchByEmail(username);

			Post post = postService.fetchPostById(id).orElse(null);

			if (post != null
					&& (post.getAuthor().getName().equals(user.getName()) || user.getRole().equals("ROLE_ADMIN"))) {
				model.addAttribute("post", post);

				String stringOfTags = postService.stringOfTags(post);
				if (!stringOfTags.isEmpty()) {
					model.addAttribute("tag", stringOfTags.substring(0, stringOfTags.length() - 1));
				} else {
					model.addAttribute("tag", "");
				}

				model.addAttribute("isAuthorFieldDisabled", !user.getRole().equals("ROLE_ADMIN"));

				return "update";
			}
		}
		return "redirect:/post";
	}

	@PutMapping("/{id}/update")
	public String updatePost(@RequestParam("authorName") String authorName, @PathVariable int id,
			@ModelAttribute Post post, @RequestParam("updateTags") String updateTags) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();
			User user = userService.fetchByEmail(username);

			Post existingPost = postService.fetchPostById(id).orElse(null);
			if (existingPost != null) {
				String postAuthorEmail = existingPost.getAuthor().getEmail();

				System.out.println("Logged-in user email: " + username);
				System.out.println("Post author email: " + postAuthorEmail);
				System.out.println(user.getRole());

				if (postAuthorEmail.equals(username) || user.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
					postService.updatePost(id, authorName, post, updateTags);
				} else {
					System.out.println("Unauthorized attempt by: " + username);
				}
			}
		}
		return "redirect:/post";
	}

	@GetMapping("/{id}/delete")
	public String deletePost(@PathVariable int id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();
			User user = userService.fetchByEmail(username);

			Post post = postService.fetchPostById(id).orElse(null);

			if (post != null) {
				String postAuthorEmail = post.getAuthor().getEmail();
				System.out.println("Logged-in user email: " + username);
				System.out.println("Post author email: " + postAuthorEmail);
				System.out.println("User role: " + user.getRole());

				if (postAuthorEmail.equals(username) || user.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
					postService.deletePostById(id);
				} else {
					System.out.println("Unauthorized attempt to delete post by: " + username);
				}
			}
		}
		return "redirect:/post";
	}

	@GetMapping("/search")
	public String searchPosts(@RequestParam(required = false, defaultValue = "asc") String sortOrder,
			@RequestParam String query, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {
		List<Post> searchResults = postService.searchPosts(query);
		model.addAttribute("allPost", searchResults);
		model.addAttribute("allPostForDate", postService.fetchAllPostsForDistinctDates());

		Page<Post> paginatedPosts = postService.getPaginatedPosts(page, size);

		model.addAttribute("pagePosts", paginatedPosts.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", (int) Math.ceil(searchResults.size() / 6));

		model.addAttribute("allTags", postService.fatchAllTagPosts());
		model.addAttribute("allAuthors", postService.fetchAllPostsForDistinctAuthor());
		model.addAttribute("sortOrder", sortOrder);

		return "index";
	}

	@GetMapping("/filterBy")
	public String filter(@RequestParam(required = false, defaultValue = "asc") String sortOrder,
			@RequestParam(required = false) List<String> tagId, @RequestParam(required = false) String selectedDate,
			Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(required = false) List<String> author) {

		List<Post> filter = postService.filter(tagId, selectedDate, author, page, size);
		Page<Post> paginatedPosts = postService.getPaginatedPosts(page, size);

		model.addAttribute("pagePosts", paginatedPosts.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", (int) Math.ceil((double) filter.size() / size));

		model.addAttribute("allPostForDate", postService.fetchAllPostsForDistinctDates());
		model.addAttribute("allTags", postService.fatchAllTagPosts());
		model.addAttribute("allAuthors", postService.fetchAllPostsForDistinctAuthor());

		model.addAttribute("selectedAuthors", author);
		model.addAttribute("selectedTags", tagId);
		model.addAttribute("sortOrder", sortOrder);

		model.addAttribute("filteredPosts", filter);

		if ((tagId == null || tagId.isEmpty()) && (selectedDate == null || selectedDate.trim().isEmpty())
				&& (author == null || author.isEmpty())) {
			return "redirect:/post";
		}
		return "index";
	}

	@GetMapping("/sortByDate")
	public String sortByDate(@RequestParam(required = false, defaultValue = "asc") String sortOrder, Model model,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {

		PageRequest pageRequest = PageRequest.of(page, size,
				Sort.by(Sort.Direction.fromString(sortOrder), "publishedAt"));

		Page<Post> paginatedPosts = postService.getPaginatedPosts(pageRequest);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginatedPosts.getTotalPages());
		model.addAttribute("allPost", paginatedPosts.getContent());

		model.addAttribute("allTags", postService.fatchAllTagPosts());
		model.addAttribute("allPostForDate", postService.fetchAllPostsForDistinctDates());
		model.addAttribute("allAuthors", postService.fetchAllPostsForDistinctAuthor());
		model.addAttribute("sortOrder", sortOrder);

		return "index";
	}
}
