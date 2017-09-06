package logic;

public interface AuthenticationEvents {
	enum eventType{
		// Login:
		LOGIN_ATTEMPT, USERNAME_NOT_FOUND, PASSWORD_DENIED,
		// Sign Up:
		SIGNUP_ATTEMPT, NAME_OCCUPIED, SECRET_DENIED,
		// General:
		CONNECTION_GRANTED
	}
}
