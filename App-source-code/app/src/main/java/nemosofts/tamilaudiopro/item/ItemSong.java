package nemosofts.tamilaudiopro.item;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ItemSong implements Serializable {

	String id;
	String artist;
	String url;
	String audioUrlHigh;
	String audioUrlLow;
	String imageBig;
	String title;
	String description;
	String lyrics;
	String averageRating="0";
	String views;
	String downloads;
	String userRating="";
	String tempName;
	Bitmap image;
	Boolean isSelected = false;
	Boolean isFavourite = false;
	Boolean isDownload = false;

	private String userMessage="" ;

	public ItemSong(String id, String artist, String url, String audioUrlHigh, String audioUrlLow,
					String imageBig, String title, String description, String lyrics,
					String averageRating, String views, String downloads, Boolean isFavourite) {
		this.id = id;
		this.artist = artist;
		this.audioUrlHigh = audioUrlHigh;
		this.audioUrlLow = audioUrlLow;
		this.url = url;
		this.imageBig = imageBig;
		this.title = title;
		this.description = description;
		this.lyrics = lyrics;
		this.averageRating = averageRating;
		this.views = views;
		this.downloads = downloads;
		this.isFavourite = isFavourite;
	}

	public ItemSong(String id,
					String artist, String url, Bitmap image, String title, String description) {
		this.id = id;
		this.artist = artist;
		this.url = url;
		this.image = image;
		this.title = title;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getArtist() {
		return artist;
	}

	public String getUrl() {
		return url;
	}

	public String getAudioUrlHigh() {
		return audioUrlHigh;
	}

	public String getAudioUrlLow() {
		return audioUrlLow;
	}

	public String getImageBig() {
		return imageBig;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLyrics() {
		return lyrics;
	}

	public Bitmap getBitmap() {
		return image;
	}

	public String getAverageRating() {
		return averageRating;
	}

	public String getViews() {
		return views;
	}

	public String getDownloads() {
		return downloads;
	}

	public String getUserRating() {
		return userRating;
	}

	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}

	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}

	public Boolean getSelected() {
		return isSelected;
	}

	public void setSelected(Boolean selected) {
		isSelected = selected;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setImageBig(String imageBig) {
		this.imageBig = imageBig;
	}

	public void setTempName(String tempName) {
		this.tempName = tempName;
	}

	public String getTempName() {
		return tempName;
	}

	public Boolean getIsFavourite() {
		return isFavourite;
	}

	public void setIsFavourite(Boolean favourite) {
		isFavourite = favourite;
	}

	public void setDownload(Boolean dow) {
		isDownload = dow;
	}
	public Boolean getIsDownload() {
		return isDownload;
	}

	public String getUserMessage() {
		return userMessage;
	}
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
}