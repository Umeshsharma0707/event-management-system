package com.event.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event.model.User;

public interface UserRepo extends JpaRepository<User, Long>{

	User findByEmail(String username);

}
