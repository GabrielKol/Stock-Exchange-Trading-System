package logic;

public class Trader {

	private String mUsername;
	private int mPasswordHash; // for security reasons, never saving the password, only its hash
	private String mFirstName;
	private String mLastName;
	private String mBankSecret;
	private String mBankAccountId;
	private String mEmail;
	private String mPhone;

	// Constructor used for SignUp:
	public Trader(String Username, int PasswordHash, String FirstName, String LastName,
			String bankSecret, String bankAccountId, String Email, String Phone) {
		this.mUsername = Username;
		this.mPasswordHash = PasswordHash;
		this.mFirstName = FirstName;
		this.mLastName = LastName;
		this.mBankSecret = bankSecret;
		this.mBankAccountId = bankAccountId;
		this.mEmail = Email;
		this.mPhone = Phone;
	}

	// Constructor used for Login:
	public Trader(String username, int passwordHash) {
		this.mUsername = username;
		this.mPasswordHash = passwordHash;
	}

	// Getters:
	public String getUsername() {
		return mUsername;
	}
	public int getPasswordHash() {
		return mPasswordHash;
	}
	public String getFirstName() {
		return mFirstName;
	}
	public String getLastName() {
		return mLastName;
	}
	public String getBankSecret() {
		return mBankSecret;
	}
	public String getBankAccountId() {
		return mBankAccountId;
	}
	public String getEmail() {
		return mEmail;
	}
	public String getPhone() {
		return mPhone;
	}

}
