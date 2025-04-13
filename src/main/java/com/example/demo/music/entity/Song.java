package com.example.demo.music.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private Integer duration;
    private String fileUrl;
    private String thumbnailUrl;
    private String genre;

    public Song() {}

    public Song(String title, String artist, Integer duration, String fileUrl, String thumbnailUrl, String genre) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.fileUrl = fileUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.genre = genre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}