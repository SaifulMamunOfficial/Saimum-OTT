package nemosofts.tamilaudiopro.item;

import java.io.Serializable;

public class ItemAlbums implements Serializable {

	String id;
	String name;
	String image;
	String totalSongs;

	public ItemAlbums(String id, String name, String image, String totalSongs) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.totalSongs = totalSongs;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public String getTotalSongs() {
		return totalSongs;
	}
}