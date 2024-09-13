package com.hari.main.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hari.main.model.User;


public interface UserRepository extends JpaRepository<User, Integer> {
	public User findUserByEmail(String email);

	public User findByName(String authorName);
}
