package com.example.footballapplication.news;

public class News {
    private final String title, image, url, time;

    public News(String title, String image, String url, String time) {
        this.title = title;
        this.image = image;
        this.url = url;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

}
