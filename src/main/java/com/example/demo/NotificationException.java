package com.example.demo;

public class NotificationException extends Exception{

	public NotificationException(String message, Exception e) {
		super(message,e);
	}
}
