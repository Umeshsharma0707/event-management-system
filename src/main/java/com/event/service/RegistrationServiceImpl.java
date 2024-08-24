package com.event.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.model.Registration;
import com.event.repos.RegistrationRepo;

@Service
public class RegistrationServiceImpl implements RegistrationService{
	
	@Autowired
	private RegistrationRepo registrationRepo;

	@Override
	public boolean doRegistration(Registration registration) {
		Registration save = this.registrationRepo.save(registration);
		
		if(save != null) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public boolean checkRegistrationByUserIdAndEventId(long userId, long eventId) {
		
		Registration registration = this.registrationRepo.findByUserIdAndEventId(userId, eventId);
		
		if(registration != null) {
			return true;
		}else {
			return false;
		}
		
		
	}

	@Override
	public List<Registration> getRegistrationsForUserEvents(long userId) {
		List<Registration> registrations = this.registrationRepo.findByEvent_UserId(userId);
		return registrations;
	}

	@Override
	public boolean confirmRegistration(Registration registration) {
			Registration updateRegistration = this.registrationRepo.findById(registration.getId()).orElse(null);
			
			if(updateRegistration != null) {
				updateRegistration.setStatus("confirm");
				this.registrationRepo.save(updateRegistration);
				return true;
			}
			
		return false;
	}

	@Override
	public Registration getRegistrationById(long registerId) {
		return this.registrationRepo.findById(registerId).orElse(null);
	}

	@Override
	public boolean cancelRegistration(Registration registration) {
		
		if(registration != null) {
			registration.setId(registration.getId());
			registration.setStatus("canceled");
			this.registrationRepo.save(registration);
			return true;
		}
		else {
			return false;
		}
	}

	

}
