package logic;

public enum transactionType{
	BID, ASK;

	@Override
	public String toString(){
		switch(this) {
			default:
			case BID:
				return "Bid";
			case ASK:
				return "Ask";
		}
	}

}