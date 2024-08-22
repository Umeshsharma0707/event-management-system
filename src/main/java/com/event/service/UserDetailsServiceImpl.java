package com.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.event.model.User;
import com.event.repos.UserRepo;

public class UserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	private UserRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		
		User user = this.userRepo.findByEmail(username);
		if(user == null) {
			System.out.println("user not found");
			throw new UsernameNotFoundException("user not found");
		}
		
		
		return new UserPrincipals(user);
	}

}
