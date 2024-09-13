package com.event.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.model.Registration;
import com.event.model.User;
import com.event.repos.RegistrationRepo;
import com.event.repos.UserRepo;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private RegistrationRepo registrationRepo;
	
	@Override
	public User insertUser(User user) {
		
		return this.userRepo.save(user);
		
	}

	@Override
	public User getUserByEmail(String email) {
		User user = this.userRepo.findByEmail(email);
		
		return user;
		
	}

	@Override
	public User getUserById(long userId) {
		User user = this.userRepo.findById(userId).orElse(null);
		return user;
	}

	@Override
	public User updateUser(User user) {
		User user2 = this.userRepo.save(user);
		return user2;
	}

	@Override
	public boolean deleteUser(User user) {
		this.userRepo.delete(user);
		return true;
	}

	@Override
	public List<Registration> getAllConfirmedStatus(String status, long userId) {
		List<Registration> byStatusAndUserId = this.registrationRepo.findByStatusAndUserId(status, userId);
		System.out.println(byStatusAndUserId);
		return byStatusAndUserId;
	}

}
