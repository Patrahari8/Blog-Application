package com.hari.main.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hari.main.model.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

	List<Post> findByAuthorNameContainingIgnoreCase(String authorName, Pageable pageable);

	List<Post> findByTagsNameIn(List<String> tags);

	List<Post> findByPublishedAt(LocalDateTime selectedDate);

	List<Post> findAllByOrderByPublishedAtAsc();

	List<Post> findAllByOrderByPublishedAtDesc();

	Page<Post> findAll(Pageable pageable);

	Page<Post> findByTagsNameIn(List<String> tagId, Pageable pageable);

	Page<Post> findByCreatedAt(LocalDate selectedDate, Pageable pageable);

	Page<Post> findByAuthorNameIn(List<String> author, Pageable pageable);

	Page<Post> findByTagsNameInAndCreatedAtAndAuthorNameIn(List<String> tagId, LocalDate selectedDate,
			List<String> author, Pageable pageable);

	Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrExcerptContainingIgnoreCaseOrAuthorNameContainingIgnoreCaseOrTagsNameContainingIgnoreCase(
	        String title, String content, String excerpt, String authorName, String tagsName, Pageable pageable);


}
