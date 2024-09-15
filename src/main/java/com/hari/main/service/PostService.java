package com.hari.main.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.hari.main.dao.PostRepository;
import com.hari.main.model.Post;
import com.hari.main.model.Tag;
import com.hari.main.model.User;

@Service
public class PostService {
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private TagService tagService;

	@Autowired
	private UserService userService;

	@Transactional
	public void savePost(User author, Post post, String tags) {
		post.setCreatedAt(LocalDate.now());
		post.setPublishedAt(LocalDateTime.now());
		post.setUpdatedAt(LocalDate.now());
		post.setAuthor(author);
		post.setPublished(true);
		
		String content = post.getContent();	
		String contentExcerpt = content.length() > 25 ? content.substring(0, content.lastIndexOf(' ', 25)) + "..." : content;
		post.setExcerpt(contentExcerpt);
		System.out.println("this is the tags "+tags);
		post.setTags(processTags(tags));
		postRepository.save(post);
	}

	public Set<Post> fetchAllPostsForDistinctDates() {
		Set<LocalDateTime> seenDates = new HashSet<>();
		Set<Post> uniquePosts = new HashSet<>();

		for (Post post : postRepository.findAll()) {
			LocalDateTime publishedAt = post.getPublishedAt();
			if (seenDates.add(publishedAt)) {
				uniquePosts.add(post);
			}
		}
		return uniquePosts;
	}

	public Set<Post> fetchAllPostsForDistinctAuthor() {
		Set<String> seenAuthors = new HashSet<>();
		Set<Post> uniqueAuthorPosts = new HashSet<>();

		for (Post post : postRepository.findAll()) {
			User author = post.getAuthor();

			String authorName = author.getName();

			if (seenAuthors.add(authorName)) {
				uniqueAuthorPosts.add(post);
			}
		}
		return uniqueAuthorPosts;
	}

	public Optional<Post> fetchPostById(int id) {
		return postRepository.findById(id);
	}

	@Transactional
	public void updatePost(int id, String authorName, Post post, String tags) {
		Post existingPost = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));

		existingPost.setTitle(post.getTitle());
		existingPost.setExcerpt(post.getExcerpt());
		existingPost.setContent(post.getContent());
		existingPost.setUpdatedAt(LocalDate.now());

		existingPost.setTags(processTags(tags));

		if (authorName != null && !authorName.isEmpty()) {
			User author = userService.fetchByName(authorName);
			if (author != null) {
				existingPost.setAuthor(author);
			}
		}

		postRepository.save(existingPost);
	}

	@Transactional
	public void deletePostById(int id) {
		Post post = postRepository.findById(id).get();
		postRepository.delete(post);
	}

	public Page<Post> searchPosts(String query, Pageable pageable) {
		return postRepository
				.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrExcerptContainingIgnoreCaseOrAuthorNameContainingIgnoreCaseOrTagsNameContainingIgnoreCase(
						query, query, query, query, query, pageable);
	}

	public Set<Tag> fatchAllTagPosts() {
		Set<Tag> tags = new HashSet<>();
		for (Post post : postRepository.findAll()) {
			for (Tag tag : post.getTags()) {
				tags.add(tag);
			}
		}
		return tags;
	}

	public Page<Post> getPaginatedPosts(Pageable pageable) {
		return postRepository.findAll(pageable);
	}

	public String stringOfTags(Post post) {
		String tag = "";
		for (Tag t : post.getTags()) {
			tag += t.getName() + ",";
		}
		return tag;

	}

	public Page<Post> filter(List<String> tagId, String selectedDate, List<String> author, String sortOrder,
			Pageable pageable) {
	
		LocalDate publishedDate = null;
		if (selectedDate != null && !selectedDate.trim().isEmpty()) {
			publishedDate = LocalDate.parse(selectedDate);
			System.out.println("dvadvdv "+ publishedDate);
		}

		if (tagId != null && !tagId.isEmpty() && publishedDate != null && author != null && !author.isEmpty()) {
			return postRepository.findByTagsNameInAndCreatedAtAndAuthorNameIn(tagId, publishedDate, author, pageable);
		} else if (tagId != null && !tagId.isEmpty()) {
			return postRepository.findByTagsNameIn(tagId, pageable);
		} else if (publishedDate != null) {
			return postRepository.findByCreatedAt(publishedDate, pageable);
		} else if (author != null && !author.isEmpty()) {
			return postRepository.findByAuthorNameIn(author, pageable);
		} else {
			return postRepository.findAll(pageable);
		}
	}
	
    public boolean isAdmin(User user) {
        return user != null && user.getRole().equalsIgnoreCase("ROLE_ADMIN");
    }
    
    public void addCommonAttributes(Model model, Pageable pageable) {
        model.addAttribute("allTags", fatchAllTagPosts());
        model.addAttribute("allPostForDate", fetchAllPostsForDistinctDates());
        model.addAttribute("allAuthors", fetchAllPostsForDistinctAuthor());
        model.addAttribute("currentPage", pageable.getPageNumber());
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.fetchByEmail(userDetails.getUsername());
        }
        return null;
    }
    
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
               && !(authentication.getPrincipal() instanceof String);
    }
    
    private Set<Tag> processTags(String tags) {
        Set<Tag> postTagList = new HashSet<>();
        String[] tagArray = tags.split(",");
        for (String tagName : tagArray) {
            tagName = tagName.trim();
            Tag tag = tagService.fetchTagsByName(tagName);
            if (tag == null) {  
                tag = new Tag();
                tag.setName(tagName);
				tag.setCreated_at(LocalDate.now());
				tag.setUpdated_at(LocalDate.now());
                tagService.saveTag(tag);
            }
            postTagList.add(tag);
        }
        return postTagList;
    }



}
