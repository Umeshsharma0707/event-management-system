package com.event.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event.model.Registration;


public interface RegistrationRepo extends JpaRepository<Registration, Long>{
			 Registration findByUserIdAndEventId(long userId,long eventId);
			 List<Registration> findByEvent_UserId(Long userId);
			 Registration findByIdAndUserId(long userId, long registerId);
			 List<Registration> findByEventId(long eventId);
			 List<Registration> findByStatusAndUserId(String status, long userId);
}
