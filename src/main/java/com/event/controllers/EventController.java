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
import com.event.model.Registration;
import com.event.model.User;
import com.event.repos.UserRepo;
import com.event.service.EventService;
import com.event.service.MailService;
import com.event.service.RegistrationService;
import com.event.service.UserPrincipals;
import com.event.service.UserService;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/events/{id}")
public class EventController {

	private String uploadDir = Paths.get("src", "main", "resources", "static", "uploads").toAbsolutePath().toString();
	private String defautImg = Paths.get("src", "main", "resources", "static", "uploads","default-img.jpg").toAbsolutePath().toString();
	@Autowired
	private UserRepo userRepo;

	@Autowired
	private EventService eventService;

	@Autowired
	private RegistrationService registrationService;
	
	@Autowired
	private MailService mailService;
	
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
			
			File uploadDirFile = new File(uploadDir);
			if (!uploadDirFile.exists()) {
				uploadDirFile.mkdirs();
			}

			// Save file
			String fileName = file.getOriginalFilename();
			File filePath = new File(uploadDirFile, fileName);
			file.transferTo(filePath);

			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);
			System.out.println("filepath : " + filePath);

				
			return "/uploads/" + fileName; 
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

		
		Event event = this.eventService.getEventById(eventId);
		
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

	    //  update the event
	    try {
	        
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
		
		
		boolean hasRegistrationByEventId = this.registrationService.hasRegistrationByEventId(eventId);
			
			if(hasRegistrationByEventId == true) {
				model.addAttribute("message", "cannot delete this event, because many users registered this event");
				return "user-events";
			}
		
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
	
	@GetMapping("eventdetails/{eventId}")
	public String eventDetails(@PathVariable("id") long userId,@PathVariable("eventId") long eventId,Model model) {
		
		Event event = this.eventService.getEventById(eventId);
		
		if(event != null) {
			model.addAttribute("event", event);
		}
		
		return "event-details";
	}
	
	@PostMapping("/register/{eventId}")
	public String RegisterEvent(@PathVariable("id") long userId, @PathVariable("eventId") long eventId, Model model) {
	    boolean registrationByUserIdAndEventId = this.registrationService.checkRegistrationByUserIdAndEventId(userId, eventId);
	    
	    if (!registrationByUserIdAndEventId) {
	        Event event = this.eventService.getEvent(eventId);
	        User user = this.userRepo.getReferenceById(userId);
	        
	        if (user != null && event != null) {
	            LocalDateTime dateTime = LocalDateTime.now();
	            Registration registration = new Registration();
	            
	            registration.setEvent(event);
	            registration.setUser(user);
	            registration.setRegistrationDate(dateTime);
	            registration.setStatus("pending");
	            
	            this.registrationService.doRegistration(registration);
	            
	            model.addAttribute("event", event); // Ensure event is added to the model
	            model.addAttribute("message", "Your registration request has been sent. Please wait for the event's main reply.");
	        } else {
	            model.addAttribute("error", "Registration failed. User or event not found.");
	        }
	    } else {
	        Event event = this.eventService.getEvent(eventId); // Fetch event again for the model
	        model.addAttribute("event", event); // Ensure event is added to the model
	        model.addAttribute("message", "You are already registered! Please wait for the event's main reply.");
	    }

	    return "event-details";
	}
	
	@GetMapping("/eventregisterrequests")
	public String eventRegisterRequests(@PathVariable("id") long userId, Model model) {
		
		List<Registration> registrations = this.registrationService.getRegistrationsForUserEvents(userId);
		
		if(registrations.isEmpty()) {
			model.addAttribute("message", "no registration requests found");
		}else if(registrations != null) {
			model.addAttribute("registrations", registrations);
		}
		
		return "events-register-request";
	}
	
	@PostMapping("/confirm-registration/{registerId}")
	public String confirmRegistration(@PathVariable("id") long userId, @PathVariable("registerId") long registerId, Model model) {
	    Registration registration = this.registrationService.getRegistrationById(registerId);

	    // Check if the registration is already confirmed
	    if (registration.getStatus().equals("confirm")) {
	        model.addAttribute("message", "Registration already confirmed!");
	       
	    } else {
	        // Confirm  registration
	        boolean confirmRegistration = this.registrationService.confirmRegistration(registration);
	        
	        if (confirmRegistration) {
	            model.addAttribute("message", "Registration confirmed");
	            
	            String to = registration.getUser().getEmail();
	            String subject = "registration confirmation";
	            
	            String emailBody = "<h1>Registration Confirmed</h1>"
                        + "<h2>Dear " + registration.getUser().getName() + ",</h2>"
                        + "<h2>Your registration for the event <strong>" + registration.getEvent().getName() + "</strong> has been confirmed.</h2>"
                        + "<h2>Event Details:</h2>"
                        + "<ul>"
                        + "<li><h2>Date: " + registration.getEvent().getEventDate() + "</h2></li>"
                        + "<li><h2>Time: " + registration.getEvent().getEventTime() + "</h2></li>"
                        + "</ul>"
                        + "<h3>Enojy the event by reach on time</h3>"
                        + "<h3>Thank you for registering!</h3>";
                        
	            
	           try {
	        	   this.mailService.sendMail(to,subject,emailBody);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
			}
	            
	            
	            
	        } else {
	            model.addAttribute("message", "Registration not confirmed");
	           
	        }
	    }

	    
	    List<Registration> registrations = this.registrationService.getRegistrationsForUserEvents(userId);
	    
	    if (registrations.isEmpty()) {
	        model.addAttribute("message", "No registration requests found");
	    } else {
	        model.addAttribute("registrations", registrations);
	    }

	    return "events-register-request";
	}

	@PostMapping("/cancel-registration/{registerId}")
	public String cancelRegistration(@PathVariable("id") long userId, @PathVariable("registerId") long registerId, Model model) {
		
		Registration registration = this.registrationService.getRegistrationById(registerId);
		
		if(registration.getStatus().equals("canceled")) {
			model.addAttribute("message", "you already marked this request as \"canceled\" ");
		}else {
			boolean cancelRegistration = this.registrationService.cancelRegistration(registration);
			
			if(cancelRegistration) {
				model.addAttribute("message", "registration marked as canceled");
				
				String to = registration.getUser().getEmail();
	            String subject = "registration cancelled";
	            
	            String emailBody = "<h1>Registration Canceled</h1>"
                        + "<h2>Dear " + registration.getUser().getName() + ",</h2>"
                        + "<h2>Your registration entry for the event <strong>" + registration.getEvent().getName() + "</strong> has been cancelled by the event organisers.</h2>"
                        + "<h2>Event Details:</h2>"
                        + "<ul>"
                        + "<li><h2>Date: " + registration.getEvent().getEventDate() + "</h2></li>"
                        + "<li><h2>Time: " + registration.getEvent().getEventTime() + "</h2></li>"
                        + "</ul>"
                        + "<h2>Try to participate on other events</h2>"
                        + "<h2>Thank you for registering!</h2>";
                        
	            
	           try {
	        	   this.mailService.sendMail(to,subject,emailBody);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
			}
				
				
				
			}else {
				model.addAttribute("message", "registration not canceled!!!, try after sometime");
			}
		}
		
		
		
		 List<Registration> registrations = this.registrationService.getRegistrationsForUserEvents(userId);
		    
		    if (registrations.isEmpty()) {
		        model.addAttribute("message", "No registration requests found");
		    } else {
		        model.addAttribute("registrations", registrations);
		    }

		    return "events-register-request";
		
		
	}
	
	
}








