package com.event.service;

import java.util.List;

import com.event.model.Registration;
import com.event.model.User;

public interface UserService {

	User insertUser(User user);
	User getUserByEmail(String email);
	User getUserById(long userId);
	User updateUser(User user);
	boolean deleteUser(User user);
	List<Registration> getAllConfirmedStatus(String status, long userId);
}
