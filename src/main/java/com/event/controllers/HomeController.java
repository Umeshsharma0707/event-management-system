package com.event.controllers;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.event.model.Event;
import com.event.model.User;
import com.event.service.EventService;
import com.event.service.MailService;
import com.event.service.UserPrincipals;
import com.event.service.UserService;
import com.event.service.UserServiceImpl;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private EventService eventService;

	@Autowired
	private MailService mailService;

	@GetMapping("/index")
	public String index() {
		return "index";
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model) {
		if (error != null) {
			model.addAttribute("errorMsg", "Invalid username or password.");
		}
		if (logout != null) {
			model.addAttribute("logoutMsg", "You have been logged out successfully.");
		}
		return "login";
	}

	@GetMapping("/userHome")
	public String userHome(@AuthenticationPrincipal UserPrincipals userPrincipal, Model model) {
		User user = userPrincipal.getFullUser();

		// Check if user or user.getName() is null
		if (user == null || user.getName() == null) {
			return "index"; // Return the index view if user or user name is null
		}
		List<Event> allEvents = this.eventService.findAllEvents();

		for (Event event : allEvents) {
			event.setDescription(event.getDescription().substring(0, 150) + "...");
		}

		model.addAttribute("events", allEvents);
		model.addAttribute("user", user); // Add user to the model
		return "user-home"; // Return the user-home view
	}

	@PostMapping("/register")
	public String register(@ModelAttribute User user, Model model) {

		if (user != null) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			this.userService.insertUser(user);
			model.addAttribute("regisetedMsg", user.getName() + " you registered successfully");
		} else {
			model.addAttribute("EMsg", "try again after some time");
			return "index";
		}
		return "login";
	}

	@GetMapping("/forgot-password")
	public String forgotPassword() {
		return "forgot-password";
	}

	@PostMapping("/forgotPassword")
	public String checkForgotEmail(@RequestParam("email") String email, Model model) {

		User user = this.userService.getUserByEmail(email);

		if (user != null) {
			model.addAttribute("userId", user.getId());
			model.addAttribute("email", user.getEmail());
			Random random = new Random();

			int otp = 100000 + random.nextInt(900000);

			String to = email;
			String subject = "OTP for reset password";

			String emailBody = "<h1>Reset Password</h1>" + "<h2>Dear " + user.getName() + ",</h2>"
					+ "<h2>you forgot your password!</h2>" + "<h2>we sent you an OTP to reset your password</h2>"

					+ "<h1>OTP is " + otp + "</h1>" + "<h3>Never share otp with another person</h3>"
					+ "<h3>Regards Team Spring Event Managers!</h3>" + "<h3>Team : Umesh Sharma</h3>";

			try {
				this.mailService.sendMail(to, subject, emailBody);
				model.addAttribute("message", "otp sent to your email,check email");
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
			}

			return "otp-verification";
		} else {
			model.addAttribute("message", "email not found!!try another");
			return "forgot-password";
		}

	}

	@GetMapping("/otp-verification")
	public String otpVerification() {
		return "otp-verification";
	}

	@GetMapping("/resendotp/{email}")
	public String resendOtp(@PathVariable("email") String email, Model model) {
		User user = this.userService.getUserByEmail(email);

		if (user != null) {
			model.addAttribute("userId", user.getId());
			model.addAttribute("email", user.getEmail());
			Random random = new Random();

			int otp = 100000 + random.nextInt(900000);

			String to = email;
			String subject = "OTP for reset password";

			String emailBody = "<h1>Reset Password</h1>" + "<h2>Dear " + user.getName() + ",</h2>"
					+ "<h2>you forgot your password!</h2>" + "<h2>we sent you an OTP to reset your password</h2>"

					+ "<h1>OTP is " + otp + "</h1>" + "<h3>Never share otp with another person</h3>"
					+ "<h3>Regards Team Spring Event Managers!</h3>" + "<h3>Team : Umesh Sharma</h3>";

			try {
				this.mailService.sendMail(to, subject, emailBody);
				model.addAttribute("message", "otp resent to your email,check email");
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
			}

			return "otp-verification";
		} else {
			return "index";
		}

	}

}
