package com.event.service;

import com.event.model.User;

public interface UserService {

	User insertUser(User user);
	User getUserByEmail(String email);
	User getUserById(long userId);
	User updateUser(User user);
	boolean deleteUser(User user);
}
