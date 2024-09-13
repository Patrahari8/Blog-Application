package com.hari.main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hari.main.dao.UserRepository;
import com.hari.main.model.User;

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    User user = userRepository.findUserByEmail(username);

	    if (user == null) {
	        throw new UsernameNotFoundException("Could not find user with email: " + username);
	    }

	    return new CustomUserDetails(user);
	}


}
