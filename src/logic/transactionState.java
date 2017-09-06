package logic;

public enum transactionState {
	OPENED, CLOSED;

	@Override
	public String toString(){
		switch(this) {
			default:
			case OPENED:
				return "Opened";
			case CLOSED:
				return "Closed";
		}
	}

}
