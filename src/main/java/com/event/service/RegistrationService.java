package com.event.service;

import java.util.List;

import com.event.model.Registration;

public interface RegistrationService {
	
	boolean doRegistration(Registration registration);
	boolean checkRegistrationByUserIdAndEventId(long userId, long eventId);
	List<Registration> getRegistrationsForUserEvents(long userId);
	boolean confirmRegistration(Registration registration);
	Registration getRegistrationById(long registerId);
	boolean cancelRegistration(Registration registration);
	boolean hasRegistrationByEventId(long eventId);
}
