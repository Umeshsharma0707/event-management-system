package com.event.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.event.model.Event;
import com.event.model.User;
import com.event.service.EventService;
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
	
	@GetMapping("/index")
	public String index() {
		return "index";
	}
	  @GetMapping("/login")
	    public String login(@RequestParam(value = "error", required = false) String error,
	                        @RequestParam(value = "logout", required = false) String logout,
	                        Model model) {
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
			
			model.addAttribute("events", allEvents);
	      model.addAttribute("user", user); // Add user to the model
	      return "user-home"; // Return the user-home view
	  }

	
	
	@PostMapping("/register")
	public String register(@ModelAttribute User user, Model model) {	
		
		if(user != null)
		{
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			this.userService.insertUser(user);
			model.addAttribute("regisetedMsg", user.getName() + " you registered successfully");
		}else {
			model.addAttribute("EMsg", "try again after some time");
			return "index";
		}
		return "login";
	}
}
