package com.event.service;

import java.util.List;

import com.event.model.Event;


public interface EventService {
	void addEvent(Event event);
	List<Event> userAllEvents(long userId);
	Event getEvent(long eventId);
	
	boolean updateEvent(Event event);
	Event getEventById(long eventId);
	boolean deleteEvent(Event event);
	List<Event> findAllEvents();
}
