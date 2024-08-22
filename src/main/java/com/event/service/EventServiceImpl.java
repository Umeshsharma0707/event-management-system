package com.event.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.model.Event;
import com.event.repos.EventRepo;

@Service
public class EventServiceImpl implements EventService{
	
	@Autowired
	private EventRepo eventRepo;

	@Override
	public void addEvent(Event event) {
		this.eventRepo.save(event);
	}

	@Override
	public List<Event> userAllEvents(long userId) {
		List<Event> byUserId = this.eventRepo.findByUserId(userId);
		return byUserId;
	}

	@Override
	public Event getEvent(long eventId) {
		Event event  = this.eventRepo.findById(eventId).orElse(null);
		
		return event;
	}

	@Override
	public boolean updateEvent(Event event) {
		
			
			Event updateEvent = getEventById(event.getId());
			updateEvent.setId(event.getId());
			updateEvent.setName(event.getName());
			updateEvent.setEventDate(event.getEventDate());
			updateEvent.setEventTime(event.getEventTime());
			updateEvent.setDescription(event.getDescription());
			updateEvent.setPosterUrl(event.getPosterUrl());
			updateEvent.setUser(event.getUser());
			
			this.eventRepo.save(updateEvent);
			
			return true;
		
	}

	@Override
	public Event getEventById(long eventId) {
		Event referenceById = this.eventRepo.getReferenceById(eventId);
		return referenceById;
	}

	@Override
	public boolean deleteEvent(Event event) {
		this.eventRepo.delete(event);
		return true;
	}

	@Override
	public List<Event> findAllEvents() {
		List<Event> allEvents = this.eventRepo.findAll();
		return allEvents;
	}

}
