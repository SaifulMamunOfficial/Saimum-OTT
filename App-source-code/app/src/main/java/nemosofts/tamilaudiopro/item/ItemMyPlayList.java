package nemosofts.tamilaudiopro.item;

import java.io.Serializable;
import java.util.List;

public class ItemMyPlayList implements Serializable{

	private final String id;
	private final String name;
	private final List<String> arrayListUrl;

	public ItemMyPlayList(String id, String name, List<String> arrayListUrl) {
		this.id = id;
		this.name = name;
		this.arrayListUrl = arrayListUrl;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<String> getArrayListUrl() {
		return arrayListUrl;
	}
}
