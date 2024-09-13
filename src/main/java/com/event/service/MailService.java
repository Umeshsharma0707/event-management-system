package com.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class MailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	public void sendMail(String to, String subject, String body) {
		MimeMessage message = javaMailSender.createMimeMessage();
		
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
			
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setFrom("springeventmanagers@gmail.com");
			helper.setText(body,true);
			
			javaMailSender.send(message);
			
		} catch (MessagingException e) {
			
			e.printStackTrace();
		}
	}
	
}
