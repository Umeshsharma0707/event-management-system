package com.event.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event.model.Event;

public interface EventRepo extends JpaRepository<Event, Long>{

	List<Event> findByUserId(long userId);
	
}
