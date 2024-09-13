package com.hari.main.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hari.main.model.Post;
import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, Integer> {

	List<Post> findByTitleContainingIgnoreCase(String query);

	List<Post> findByContentContainingIgnoreCase(String query);

	List<Post> findByExcerptContainingIgnoreCase(String query);

	List<Post> findByTagsNameContainingIgnoreCase(String query);
	
    List<Post> findByAuthorNameContainingIgnoreCase(String authorName);

	List<Post> findByTagsNameIn(List<String> tags);

	List<Post> findByPublishedAt(LocalDateTime selectedDate);

	List<Post> findAllByOrderByPublishedAtAsc();

	List<Post> findAllByOrderByPublishedAtDesc();

	Page<Post> findAll(Pageable pageable);

}
