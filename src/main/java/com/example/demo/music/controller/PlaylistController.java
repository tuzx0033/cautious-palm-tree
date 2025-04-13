package com.example.demo.music.controller;

import com.example.demo.music.entity.Playlist;
import com.example.demo.music.entity.Song;
import com.example.demo.music.repository.PlaylistRepository;
import com.example.demo.music.repository.SongRepository;
import com.example.demo.music.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SupabaseStorageService storageService;

    @GetMapping
    public String showPlaylists(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Playlist> playlistPage = playlistRepository.findAll(pageable);
        Page<Song> allSongs = songRepository.findAll(PageRequest.of(0, 100));
        model.addAttribute("playlists", playlistPage.getContent());
        model.addAttribute("allSongs", allSongs.getContent());
        model.addAttribute("currentPage", playlistPage.getNumber());
        model.addAttribute("totalPages", playlistPage.getTotalPages());
        return "playlists";
    }

    @GetMapping("/{id}")
    public String showPlaylistDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Playlist playlist = playlistRepository.findByIdWithSongs(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy playlist."));
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage;
        List<Song> songs = playlist.getSongs() != null ? playlist.getSongs() : new ArrayList<>();
        int start = Math.min((int) pageable.getOffset(), songs.size());
        int end = Math.min(start + pageable.getPageSize(), songs.size());
        if (songs.isEmpty()) {
            songPage = new PageImpl<>(songs, pageable, 0);
        } else {
            songPage = new PageImpl<>(songs.subList(start, end), pageable, songs.size());
        }
        Page<Song> allSongs = songRepository.findAll(PageRequest.of(0, 100));
        model.addAttribute("playlist", playlist);
        model.addAttribute("songs", songPage.getContent());
        model.addAttribute("allSongs", allSongs.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        return "playlist_detail";
    }

    @PostMapping("/create")
    public String createPlaylist(
            @RequestParam("name") String name,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        if (name == null || name.trim().isEmpty() || name.length() > 100) {
            model.addAttribute("message", "Tên playlist không hợp lệ.");
            return showPlaylists(page, 10, model);
        }
        Playlist playlist = new Playlist(name);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                String contentType = thumbnail.getContentType();
                if (contentType != null && (contentType.startsWith("image/jpeg") || contentType.startsWith("image/png"))) {
                    String thumbnailUrl = storageService.uploadFile(thumbnail, "playlists");
                    playlist.setThumbnailUrl(thumbnailUrl);
                } else {
                    model.addAttribute("message", "Chỉ hỗ trợ định dạng JPEG hoặc PNG.");
                    return showPlaylists(page, 10, model);
                }
            } catch (IOException e) {
                model.addAttribute("message", "Lỗi khi tải ảnh lên: " + e.getMessage());
                return showPlaylists(page, 10, model);
            }
        }
        playlistRepository.save(playlist);
        model.addAttribute("message", "Đã tạo playlist: " + name);
        return showPlaylists(page, 10, model);
    }

    @PostMapping("/add-song")
    public String addSongToPlaylist(
            @RequestParam("playlistId") Long playlistId,
            @RequestParam("songId") Long songId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy playlist."));
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài hát."));
            if (playlist.getSongs().contains(song)) {
                model.addAttribute("message", "Bài hát đã có trong playlist.");
            } else {
                playlist.addSong(song);
                playlistRepository.save(playlist);
                model.addAttribute("message", "Đã thêm '" + song.getTitle() + "' vào playlist '" + playlist.getName() + "'.");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        return showPlaylistDetail(playlistId, page, 10, model);
    }

    @PostMapping("/remove-song")
    public String removeSongFromPlaylist(
            @RequestParam("playlistId") Long playlistId,
            @RequestParam("songId") Long songId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy playlist."));
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài hát."));
            playlist.removeSong(song);
            playlistRepository.save(playlist);
            model.addAttribute("message", "Đã xóa '" + song.getTitle() + "' khỏi playlist '" + playlist.getName() + "'.");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        return showPlaylistDetail(playlistId, page, 10, model);
    }

    @PostMapping("/delete")
    public String deletePlaylist(
            @RequestParam("playlistId") Long playlistId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy playlist."));
            if (playlist.getThumbnailUrl() != null) {
                String fileName = playlist.getThumbnailUrl().substring(playlist.getThumbnailUrl().lastIndexOf("/") + 1);
                storageService.deleteFile(fileName, "playlists");
            }
            playlistRepository.delete(playlist);
            model.addAttribute("message", "Đã xóa playlist '" + playlist.getName() + "'.");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        return showPlaylists(page, 10, model);
    }
}