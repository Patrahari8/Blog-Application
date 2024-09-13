package com.hari.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hari.main.model.User;
import com.hari.main.service.UserService;

@Controller
@RequestMapping
public class UserController {

	@Autowired
	UserService userService;

	@GetMapping("/signup")
	public String signUp(Model model) {
		model.addAttribute("user", new User());
		return "registration";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user) {
		user.setRole("ROLE_AUTHOR");
		user.setEnabled(true);
		userService.saveTheUser(user);

		return "redirect:/login";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

}
