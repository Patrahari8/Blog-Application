package com.hari.main.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hari.main.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

}
