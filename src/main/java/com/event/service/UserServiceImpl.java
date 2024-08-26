package com.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.model.User;
import com.event.repos.UserRepo;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepo userRepo;
	
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

}
