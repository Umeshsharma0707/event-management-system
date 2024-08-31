package com.event.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.event.model.Event;
import com.event.model.Registration;
import com.event.model.User;
import com.event.repos.RegistrationRepo;
import com.event.service.EventService;
import com.event.service.UserPrincipals;
import com.event.service.UserService;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private EventService eventService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private RegistrationRepo registrationRepo;
	
	
	@ModelAttribute
	public void addUserAttribute(@AuthenticationPrincipal UserPrincipals userPrincipal, Model model) {
		User user = userPrincipal.getFullUser();
		if (user != null) {
			model.addAttribute("user", user);
		}
		
		List<Event> allEvents = this.eventService.findAllEvents();
		
		model.addAttribute("events", allEvents);
		
	}
	
	@GetMapping("/{userId}")
	public String getUserDetails(@PathVariable("userId") long userId, Model model) {
	
		User user = this.userService.getUserById(userId);
		
		if(user!=null) {
			model.addAttribute("user", user);
			return "user-details";
		}else {
			return "login";
		}
	}
	
	@PostMapping("/update")
	public String updateUser(@ModelAttribute("user") User user, Model model) {
		
		User updateUser = this.userService.getUserById(user.getId());
		
		if(updateUser != null) {
			updateUser.setName(user.getName());
			updateUser.setPhone(user.getPhone());
			updateUser.setCity(user.getCity());
			
			User updatedUser = this.userService.updateUser(updateUser);
			

				String msg = updatedUser.getName() + " your profile is updated successfully";
				model.addAttribute("message", msg);
				return "user-details";
			
		}else {
			model.addAttribute("message", "some error occurs while updating!! login again");
			return "login";
		}
		
	}
	
	@GetMapping("/deleteaccount")
	public String deleteAccountView() {
		return "delete-account";
	}
	
	
	@PostMapping("/delete-user")
	public String deleteUser(@RequestParam("id") long userId, @RequestParam("email") String email, Model model) {
	    if (email != null && !email.isEmpty()) {
	        
	        User user = this.userService.getUserById(userId);

	        
	        if (user != null) {
	            try {
	                if (user.getEmail().equals(email)) {
	 	               
		            	
		            	List<Event> userAllEvents = this.eventService.userAllEvents(userId);
		            	
		            	if(!userAllEvents.isEmpty()) {
		            		for(Event event : userAllEvents) {
		            			List<Registration> byEventId = this.registrationRepo.findByEventId(event.getId());
		            			for(Registration registration : byEventId) {
		            				this.registrationRepo.delete(registration);
		            			}
		            			this.eventService.deleteEvent(event);
		            		}
		            	}
		            	
		                boolean userDeleted = this.userService.deleteUser(user);
		                
		                
		                if (userDeleted) {
		                    model.addAttribute("message", "Your account has been deleted. You can create a new account.");
		                    return "account-deleted";
		                } else {
		                    model.addAttribute("message", "There was an issue deleting your account. Please try again.");
		                    return "delete-account";
		                }
		            } else {
		                model.addAttribute("message", "The entered text not matching,try again");
		                return "delete-account";
		            }
				} catch (Exception e) {
					model.addAttribute("message", e.getMessage());
					return "delete-account";
				}
	        } else {
	            
	            model.addAttribute("message", "User not found. Please log in.");
	            return "login";
	        }
	    } else {
	        
	        model.addAttribute("message", "Please enter the text that shown in red color");
	        return "delete-account";
	    }
	}

	
	/* end of user controller */
}
