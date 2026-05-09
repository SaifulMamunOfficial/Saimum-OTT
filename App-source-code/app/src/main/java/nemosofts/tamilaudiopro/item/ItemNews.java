package nemosofts.tamilaudiopro.item;


import java.io.Serializable;

public class ItemNews implements Serializable {

    private final String id;
    private final String newsName;
    private final String newsUrl;

    public ItemNews(String id, String newsName, String newsUrl) {
        this.id = id;
        this.newsName = newsName;
        this.newsUrl = newsUrl;
    }

    public String getId() {
        return id;
    }

    public String getNewsName() {
        return newsName;
    }

    public String getNewsUrl() {
        return newsUrl;
    }
}