package nemosofts.tamilaudiopro.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemPost implements Serializable{

	String id;
	String title;
	String type;
	Boolean isSections;
	private final List<ItemHomeSlider> arrayListBanner = new ArrayList<>();
	private final List<ItemAlbums> arrayListAlbums = new ArrayList<>();
	private final List<ItemArtist> arrayListArtist = new ArrayList<>();
	private final List<ItemSong> arrayListSongs = new ArrayList<>();
	private final List<ItemServerPlayList> arrayListPlaylist = new ArrayList<>();
	private final List<ItemCat> arrayListCategories = new ArrayList<>();

	public ItemPost(String id, String title, String type, Boolean isSections) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.isSections = isSections;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public Boolean getIsSections() {
		return isSections;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<ItemHomeSlider> getArrayListBanner() {
		return arrayListBanner;
	}
	public void setArrayListBanner(List<ItemHomeSlider> arrayListBanner) {
		this.arrayListBanner.addAll(arrayListBanner);
	}

	public List<ItemAlbums> getArrayListAlbums() {
		return arrayListAlbums;
	}
	public void setArrayListAlbums(List<ItemAlbums> arrayListAlbums) {
		this.arrayListAlbums.addAll(arrayListAlbums);
	}

	public List<ItemArtist> getArrayListArtist() {
		return arrayListArtist;
	}
	public void setArrayListArtist(List<ItemArtist> arrayListArtist) {
		this.arrayListArtist.addAll(arrayListArtist);
	}

	public List<ItemSong> getArrayListSongs() {
		return arrayListSongs;
	}
	public void setArrayListSongs(List<ItemSong> arrayListSongs) {
		this.arrayListSongs.addAll(arrayListSongs);
	}

	public List<ItemServerPlayList> getArrayListPlaylist() {
		return arrayListPlaylist;
	}
	public void setArrayListPlaylist(List<ItemServerPlayList> arrayListPlaylist) {
		this.arrayListPlaylist.addAll(arrayListPlaylist);
	}

	public List<ItemCat> getArrayListCategories() {
		return arrayListCategories;
	}
	public void setArrayListCategories(List<ItemCat> arrayListCategories) {
		this.arrayListCategories.addAll(arrayListCategories);
	}
}