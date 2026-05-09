package nemosofts.tamilaudiopro.item;

import java.io.Serializable;

public class ItemRating implements Serializable {

	private final String id;
	private final String rate;
	private final String message;
	private final String userName;
	private final String dp;

	public ItemRating(String id, String rate, String message, String userName, String dp) {
		this.id = id;
		this.rate = rate;
		this.message = message;
		this.userName = userName;
		this.dp = dp;
	}

	public String getId() {
		return id;
	}

	public String getRate() {
		return rate;
	}

	public String getMessage() {
		return message;
	}

	public String getUserName() {
		return userName;
	}

	public String getDp() {
		return dp;
	}
}