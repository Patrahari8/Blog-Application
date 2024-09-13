package com.hari.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hari.main.dao.UserRepository;
import com.hari.main.model.User;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public void saveTheUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}

	public User fetchByEmail(String email) {
		User userByEmail = userRepository.findUserByEmail(email);
		return userByEmail;
	}

	public User fetchByName(String authorName) {
		User user = userRepository.findByName(authorName);
		return user;
	}
}
