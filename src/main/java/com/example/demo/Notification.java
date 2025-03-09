package com.example.demo;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="notifcation")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long orderId;
	
	@Value("${twilio.phone.number}")
	private String twilioPhoneNumber;
	private Long customerId;
	
	private String orderReference;
	
	private String email;
	private String mobileNumber;
	
	private String message;
	
    @Enumerated(EnumType.STRING)
	private Notificationtype type;
	
	private boolean isSent;
	
	private LocalDateTime sentAt;
	
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    
    
	/**
	 * @return the twilioPhoneNumber
	 */
	public String getTwilioPhoneNumber() {
		return twilioPhoneNumber;
	}



	/**
	 * @param twilioPhoneNumber the twilioPhoneNumber to set
	 */
	public void setTwilioPhoneNumber(String twilioPhoneNumber) {
		this.twilioPhoneNumber = twilioPhoneNumber;
	}



	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the orderId
	 */
	public Long getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the customerId
	 */
	public Long getCustomerId() {
		return customerId;
	}

	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	/**
	 * @return the orderReference
	 */
	public String getOrderReference() {
		return orderReference;
	}

	/**
	 * @param orderReference the orderReference to set
	 */
	public void setOrderReference(String orderReference) {
		this.orderReference = orderReference;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the mobileNumber
	 */
	public String getMobileNumber() {
		return mobileNumber;
	}

	/**
	 * @param mobileNumber the mobileNumber to set
	 */
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the type
	 */
	public Notificationtype getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Notificationtype type) {
		this.type = type;
	}

	/**
	 * @return the isSent
	 */
	public boolean isSent() {
		return isSent;
	}

	/**
	 * @param isSent the isSent to set
	 */
	public void setSent(boolean isSent) {
		this.isSent = isSent;
	}

	/**
	 * @return the sentAt
	 */
	public LocalDateTime getSentAt() {
		return sentAt;
	}

	/**
	 * @param sentAt the sentAt to set
	 */
	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	
    /**
	 * @param id
	 * @param orderId
	 * @param twilioPhoneNumber
	 * @param customerId
	 * @param orderReference
	 * @param email
	 * @param mobileNumber
	 * @param message
	 * @param type
	 * @param isSent
	 * @param sentAt
	 */
	public Notification(Long id, Long orderId, String twilioPhoneNumber, Long customerId, String orderReference,
			String email, String mobileNumber, String message, Notificationtype type, boolean isSent,
			LocalDateTime sentAt) {
		super();
		this.id = id;
		this.orderId = orderId;
		this.twilioPhoneNumber = twilioPhoneNumber;
		this.customerId = customerId;
		this.orderReference = orderReference;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.message = message;
		this.type = type;
		this.isSent = isSent;
		this.sentAt = sentAt;
	}



	@Override
	public int hashCode() {
		return Objects.hash(customerId, email, id, isSent, message, mobileNumber, orderId, orderReference, sentAt,
				twilioPhoneNumber, type);
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notification other = (Notification) obj;
		return Objects.equals(customerId, other.customerId) && Objects.equals(email, other.email)
				&& Objects.equals(id, other.id) && isSent == other.isSent && Objects.equals(message, other.message)
				&& Objects.equals(mobileNumber, other.mobileNumber) && Objects.equals(orderId, other.orderId)
				&& Objects.equals(orderReference, other.orderReference) && Objects.equals(sentAt, other.sentAt)
				&& Objects.equals(twilioPhoneNumber, other.twilioPhoneNumber) && type == other.type;
	}



	public Notification() {
    	
    }
}
