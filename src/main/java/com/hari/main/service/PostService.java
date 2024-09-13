package com.hari.main.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hari.main.dao.PostRepository;
import com.hari.main.dao.UserRepository;
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
		if (content.length() > 25) {
			post.setExcerpt(content.substring(0, 25) + "...");
		} else {
			post.setExcerpt(content);
		}
		System.out.println(tags);
		Set<Tag> postTagList = new HashSet<>();
		String[] tagArray = tags.split(",");
		for (String tagName : tagArray) {
			tagName = tagName.trim();
			Tag tag = tagService.fetchTagsByName(tagName);
			if (tag == null) {
				Tag newTag = new Tag();
				newTag.setName(tagName);
				newTag.setCreated_at(LocalDate.now());
				newTag.setUpdated_at(LocalDate.now());
				tagService.saveTag(newTag);
				postTagList.add(newTag);
			} else {
				postTagList.add(tag);
			}
		}

		post.setTags(postTagList);

		postRepository.save(post);
	}

	public List<Post> fatchAllPosts() {
		return postRepository.findAll();
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

		String[] tagStrings = tags.split(",");
		Set<Tag> tempSet = new HashSet<>();

		for (String tagName : tagStrings) {
			Tag tag = tagService.fetchTagsByName(tagName);

			if (tag == null) {
				Tag insertTag = new Tag();
				insertTag.setName(tagName);
				insertTag.setCreated_at(LocalDate.now());
				insertTag.setUpdated_at(LocalDate.now());
				tempSet.add(insertTag);
				tagService.saveTag(insertTag);
			} else {
				tag.setUpdated_at(LocalDate.now());
				tempSet.add(tag);
			}
		}
		existingPost.setTags(tempSet);

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

	public List<Post> searchPosts(String query) {
		Set<Post> searchedDataSet = new HashSet<>();
		searchedDataSet.addAll(postRepository.findByTitleContainingIgnoreCase(query));
		searchedDataSet.addAll(postRepository.findByExcerptContainingIgnoreCase(query));
		searchedDataSet.addAll(postRepository.findByContentContainingIgnoreCase(query));
		searchedDataSet.addAll(postRepository.findByTagsNameContainingIgnoreCase(query));
		searchedDataSet.addAll(postRepository.findByAuthorNameContainingIgnoreCase(query));
		return new ArrayList<>(searchedDataSet);
	}

	public List<Post> filterPostsByTags(List<String> selectedTags) {
		return postRepository.findByTagsNameIn(selectedTags);
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

	public List<Post> getPostsSortedByDateAsc() {
		return postRepository.findAllByOrderByPublishedAtAsc();
	}

	public List<Post> getPostsSortedByDateDesc() {
		return postRepository.findAllByOrderByPublishedAtDesc();
	}

	public Page<Post> getPaginatedPosts(int page, int size) {
		return postRepository.findAll(PageRequest.of(page, size));
	}

	public Page<Post> getPaginatedPosts(PageRequest pageRequest) {
		return postRepository.findAll(pageRequest);
	}

	public String stringOfTags(Post post) {
		String tag = "";
		for (Tag t : post.getTags()) {
			tag += t.getName() + ",";
		}
		return tag;

	}

	public List<Post> filter(List<String> tagId, String selectedDate, List<String> author, int page, int size) {
		List<Post> filteredPosts = new ArrayList<>();

		if (tagId != null && !tagId.isEmpty()) {
			filteredPosts = filterPostsByTags(tagId);
		} else {
			filteredPosts = fatchAllPosts();
		}

		if (selectedDate != null && !selectedDate.trim().isEmpty()) {
			filteredPosts = filterPostsByDateInList(filteredPosts, selectedDate);
		}

		if (author != null && !author.isEmpty()) {
			filteredPosts = filterPostsByAuthorInList(filteredPosts, author);
		}

		return filteredPosts;

	}

	public List<Post> filterPostsByDateInList(List<Post> posts, String selectedDate) {
		LocalDate date = LocalDate.parse(selectedDate);

		return posts.stream().filter(post -> post.getPublishedAt().toLocalDate().equals(date))
				.collect(Collectors.toList());
	}

	public List<Post> filterPostsByAuthorInList(List<Post> posts, List<String> authors) {
		return posts.stream().filter(post -> post.getAuthor() != null && authors.contains(post.getAuthor().getName()))
				.collect(Collectors.toList());
	}

}
