package com.event.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.event.model.Event;
import com.event.model.User;
import com.event.repos.UserRepo;
import com.event.service.EventService;
import com.event.service.UserPrincipals;
import com.event.service.UserService;

@Controller
@RequestMapping("/events/{id}")
public class EventController {

	private String uploadDir = Paths.get("src", "main", "resources", "static", "uploads").toAbsolutePath().toString();

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private EventService eventService;

	@ModelAttribute
	public void addUserAttribute(@AuthenticationPrincipal UserPrincipals userPrincipal, Model model) {
		User user = userPrincipal.getFullUser();
		if (user != null) {
			model.addAttribute("user", user);
		}
		
		List<Event> allEvents = this.eventService.findAllEvents();
		
		model.addAttribute("events", allEvents);
		
	}

	
	@GetMapping("/userHome")
	public String userHomePage() {
		return "user-home";
	}
	
	@GetMapping("/addevent")
	public String addEventForm(@PathVariable("id") long id, @AuthenticationPrincipal UserPrincipals userPrincipal,
			Model model) {

		User user = userPrincipal.getFullUser();

		model.addAttribute("user", user);

		if (user == null) {
			System.out.println("user is null");
			return "login";
		}

		return "add-event";
	}

	@PostMapping("/add")
	public String add(@PathVariable("id") long id, @RequestParam("userId") long userId,
			@RequestParam("name") String name, @RequestParam("eventDate") String eventDate,
			@RequestParam("eventTime") String eventTime, @RequestParam("description") String description,
			 @RequestParam("location") String location,
			@RequestParam("poster") MultipartFile poster, Model model,
			@AuthenticationPrincipal UserPrincipals userPrincipals) {

		// Get the authenticated user
		User user = userPrincipals.getFullUser();
		model.addAttribute("user", user);

		// Check if user is null
		if (user == null) {
			System.out.println("User is null");
			return "login";
		}

		// Validate date and time
		LocalDate date;
		LocalTime time;
		try {
			date = LocalDate.parse(eventDate);
			time = LocalTime.parse(eventTime);
		} catch (DateTimeParseException e) {
			model.addAttribute("error", "Invalid date or time format.");
			return "add-event";
		}

		LocalDateTime eventDateTime = LocalDateTime.of(date, time);
		LocalDateTime now = LocalDateTime.now();

		// Check if event date and time are in the future
		if (eventDateTime.isBefore(now)) {
			model.addAttribute("error", "Event date and time must be in the future.");
			return "add-event";
		}

		// Handle file upload
		if (poster.isEmpty()) {
			model.addAttribute("error", "Please select a file.");
			return "add-event";
		}

		String posterUrl = savePoster(poster);
		System.out.println("poster url : " + posterUrl);
		// Create and save the event
		try {
			Event event = new Event();
			event.setName(name);
			event.setEventDate(date);
			event.setEventTime(time);
			event.setDescription(description);
			event.setLocation(location);
			event.setPosterUrl(posterUrl);
			event.setUser(user);

			List<Event> events = new ArrayList<Event>();
			events.add(event);
			user.setEvents(events);
			this.userRepo.save(user);

			// Save the event to the database
			this.eventService.addEvent(event);

			model.addAttribute("message", "Event added successfully!");

			List<Event> userAllEvents = this.eventService.userAllEvents(userId);

			model.addAttribute("events", userAllEvents);

			return "user-events"; // Redirect to another page after success
		} catch (Exception e) {
			model.addAttribute("error", "Error adding event: " + e.getMessage());
			return "add-event";
		}
	}

	private String savePoster(MultipartFile file) {
		if (file.isEmpty()) {
			return null;
		}

		try {
			// Create the upload directory if it does not exist
			File uploadDirFile = new File(uploadDir);
			if (!uploadDirFile.exists()) {
				uploadDirFile.mkdirs();
			}

			// Save the file
			String fileName = file.getOriginalFilename();
			File filePath = new File(uploadDirFile, fileName);
			file.transferTo(filePath);

			System.out.println("filepath : " + filePath);

			// Return the URL or path
			return "/uploads/" + fileName; // or filePath.toString() if you want the full path
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@GetMapping("/userEvents")
	public String userAllEvents(@PathVariable("id") long userId, Model model) {

		List<Event> userAllEvents = this.eventService.userAllEvents(userId);

		model.addAttribute("events", userAllEvents);

		return "user-events";
	}

	@GetMapping("/event/{eventId}")
	public String getEvent(@PathVariable("id") long userId, @PathVariable("eventId") long eventId, Model model) {
		Event event = this.eventService.getEvent(eventId);

		model.addAttribute("event", event);

		return "edit-event";
	}

	@PostMapping("/update/{eventId}")
	public String updateEvent(@PathVariable("id") long userId, @PathVariable("eventId") long eventId,
	        @RequestParam("name") String name,
	        @RequestParam("eventDate") String eventDate, @RequestParam("eventTime") String eventTime,
	        @RequestParam("description") String description,
	        @RequestParam("location") String location,
	        @RequestParam("poster") MultipartFile poster,
	        Model model) {

	    LocalDate date;
	    LocalTime time;
	    try {
	        date = LocalDate.parse(eventDate);
	        time = LocalTime.parse(eventTime);
	    } catch (DateTimeParseException e) {
	        model.addAttribute("error", "Invalid date or time format.");
	        return "edit-event";
	    }

	    LocalDateTime eventDateTime = LocalDateTime.of(date, time);
	    LocalDateTime now = LocalDateTime.now();

	    // Check if event date and time are in the future
	    if (eventDateTime.isBefore(now)) {
	        model.addAttribute("error", "Event date and time must be in the future.");
	        return "edit-event";
	    }

	    String posterUrl = null;
	    // Handle file upload
	    if (poster != null && !poster.isEmpty()) {
	        try {
	            posterUrl = savePoster(poster);
	        } catch (Exception e) {
	            model.addAttribute("error", "Error uploading poster: " + e.getMessage());
	            return "edit-event";
	        }
	    } else {
	        model.addAttribute("error", "Please select a poster image.");
	        return "edit-event";
	    }

	    // Create and update the event
	    try {
	        Event event = this.eventService.getEventById(eventId);
	        if (event == null) {
	            model.addAttribute("error", "Event not found.");
	            return "edit-event";
	        }
	        event.setId(eventId);
	        event.setName(name);
	        event.setEventDate(date);
	        event.setEventTime(time);
	        event.setDescription(description);
	        event.setLocation(location);
	        event.setPosterUrl(posterUrl);

	        User user = this.userRepo.findById(userId).orElse(null);
	        if (user == null) {
	            model.addAttribute("error", "User not found.");
	            return "edit-event";
	        }

	        event.setUser(user);
	        this.eventService.updateEvent(event);

	        List<Event> userAllEvents = this.eventService.userAllEvents(userId);
	        model.addAttribute("events", userAllEvents);
	        model.addAttribute("message", "Event updated successfully!");

	        return "user-events"; // Redirect to another page after success
	    } catch (Exception e) {
	        model.addAttribute("error", "Error updating event: " + e.getMessage());
	        return "edit-event";
	    }
	}
	
	@GetMapping("/deleteEvent/{eventId}")
	public String deleteEvent(@PathVariable("id") long userId,@PathVariable("eventId") long eventId,Model model) {
		
		Event event = this.eventService.getEventById(eventId);
		
		boolean deleteEvent = this.eventService.deleteEvent(event);
		
		if(deleteEvent) {
			String msg =  event.getName() + " , event is deleted";
			model.addAttribute("message", msg);
			List<Event> userAllEvents = this.eventService.userAllEvents(userId);
	        model.addAttribute("events", userAllEvents);
		}else {
			model.addAttribute("message", "event not deleted");
		}
		
		return "user-events";
		
	}
	
	/*
	 * @GetMapping("/events") public String showAllEvents(Model model) { List<Event>
	 * events = this.eventService.findAllEvents(); model.addAttribute("events",
	 * events); return "all-events"; }
	 */

}
