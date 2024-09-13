package com.hari.main.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hari.main.dao.CommentRepository;
import com.hari.main.dao.PostRepository;
import com.hari.main.model.Comment;
import com.hari.main.model.Post;

@Service
public class CommentService {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Transactional
	public void saveComment(int id, String name, String email, String newComment) {
		Post post = postRepository.findById(id).get();
		Comment comment = new Comment();
		System.out.println("this the the outpuyt" + email);
		String[] split = name.split(",");
		if (split.length == 2) {
			comment.setName(name.split(",")[1]);
		}else {
			comment.setName(name);

		}
		
		String[] splitEmail = email.split(",");
		if (splitEmail.length == 2) {
			comment.setEmail(splitEmail[1]);
		}else {
			comment.setEmail(email);

		}
		
		
		
		
		comment.setComment(newComment);
		comment.setPost(post);
		comment.setCreated_at(LocalDate.now());
		comment.setUpdated_at(LocalDate.now());
		commentRepository.save(comment);
	}

	@Transactional
	public void deleteCommentById(int id) {
		commentRepository.deleteById(id);
	}

	public Optional<Comment> editCommentBtId(int id) {
		return commentRepository.findById(id);
	}

	public Comment editCommentById(int cid) {
		return commentRepository.findById(cid).get();
	}

	@Transactional
	public void updateCommentById(int id, int postId, Comment comment) {
		Post post = postRepository.findById(postId).get();

		for (Comment com : post.getComments()) {
			if (com.getId() == id) {
				String c = comment.getComment();

				com.setComment(c);
				com.setUpdated_at(LocalDate.now());
				commentRepository.save(com);

			}
		}
		postRepository.save(post);
	}

	public Comment getCommentById(int cid) {
		
		return commentRepository.findById(cid).orElse(null);
	}

}
