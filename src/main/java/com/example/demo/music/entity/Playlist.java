package com.example.demo.music.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String thumbnailUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "playlist_songs",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

    @Column(name = "song_count")
    private Integer songCount; // Thay int bằng Integer

    public Playlist() {}
    public Playlist(String name) { 
        this.name = name; 
        this.songCount = 0; // Gán mặc định
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { 
        this.songs = songs; 
        this.songCount = songs != null ? songs.size() : 0; 
    }
    public void addSong(Song song) { 
        this.songs.add(song); 
        this.songCount = this.songs.size(); 
    }
    public void removeSong(Song song) { 
        this.songs.remove(song); 
        this.songCount = this.songs.size(); 
    }
    public Integer getSongCount() { return songCount; }
    public void setSongCount(Integer songCount) { 
        this.songCount = songCount != null ? songCount : 0; 
    }
}