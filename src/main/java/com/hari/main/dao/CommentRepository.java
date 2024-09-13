package com.hari.main.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hari.main.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

}
