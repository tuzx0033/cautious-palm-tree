package com.example.demo.music.controller;

import com.example.demo.music.entity.Playlist;
import com.example.demo.music.entity.Song;
import com.example.demo.music.repository.PlaylistRepository;
import com.example.demo.music.repository.SongRepository;
import com.example.demo.music.service.SupabaseStorageService;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/api/projects")
public class MusicProjectController {

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    private static final List<String> GENRES = Arrays.asList("Pop", "Rock", "Jazz", "Classical", "Hip Hop", "Electronic");

    @GetMapping("/music")
    public String showMusicPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "genre", required = false) String genre,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage;
        if (genre != null && !genre.isEmpty()) {
            songPage = songRepository.findByGenre(genre, pageable);
        } else {
            songPage = songRepository.findAll(pageable);
        }
        List<Playlist> playlists = playlistRepository.findAll();
        Pageable randomPageable = PageRequest.of(0, 5);
        List<Song> recommendations = songRepository.findRandomSongs(randomPageable);
        model.addAttribute("files", songPage.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        model.addAttribute("playlists", playlists);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("genres", GENRES);
        model.addAttribute("selectedGenre", genre);
        return "music";
    }

    @PostMapping("/upload")
    public String uploadMusicProject(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "genre", required = false) String genre,
            Model model) throws IOException {
        File tempFile = File.createTempFile("temp", ".mp3");
        try {
            Files.write(tempFile.toPath(), file.getBytes());
            Integer duration;
            try {
                AudioFile audioFile = AudioFileIO.read(tempFile);
                duration = audioFile.getAudioHeader().getTrackLength();
            } catch (Exception e) {
                duration = 180;
                System.out.println("Error reading audio duration: " + e.getMessage());
            }
            String fileUrl = supabaseStorageService.uploadFile(file, "audio");
            String thumbnailUrl = thumbnail != null && !thumbnail.isEmpty()
                    ? supabaseStorageService.uploadFile(thumbnail, "thumbnails")
                    : null;
            Song song = new Song(title, artist, duration, fileUrl, thumbnailUrl, genre);
            songRepository.save(song);
            model.addAttribute("message", "File uploaded successfully: " + fileUrl);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        Pageable pageable = PageRequest.of(0, 5);
        Page<Song> songPage = songRepository.findAll(pageable);
        model.addAttribute("files", songPage.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        model.addAttribute("genres", GENRES);
        return "music";
    }

    @PostMapping("/delete")
    public String deleteFile(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            // Tìm bài hát theo fileUrl chứa fileName
            Song songToDelete = songRepository.findByFileUrlContaining(fileName)
                    .orElse(null);
    
            if (songToDelete == null) {
                model.addAttribute("message", "Không tìm thấy bài hát với tên tệp: " + fileName);
                System.out.println("Bài hát không tồn tại trong cơ sở dữ liệu: " + fileName);
            } else {
                // Xóa tệp audio từ Supabase
                try {
                    supabaseStorageService.deleteFile(fileName, "audio");
                } catch (Exception e) {
                    System.out.println("Không thể xóa tệp audio từ Supabase: " + e.getMessage());
                }
    
                // Xóa thumbnail nếu có
                if (songToDelete.getThumbnailUrl() != null) {
                    String thumbnailName = songToDelete.getThumbnailUrl().substring(
                            songToDelete.getThumbnailUrl().lastIndexOf("/") + 1);
                    try {
                        supabaseStorageService.deleteFile(thumbnailName, "thumbnails");
                    } catch (Exception e) {
                        System.out.println("Không thể xóa thumbnail từ Supabase: " + e.getMessage());
                    }
                }
    
                // Xóa liên kết trong playlist_songs trước
                jdbcTemplate.update("DELETE FROM playlist_songs WHERE song_id = ?", songToDelete.getId());
    
                // Xóa bài hát khỏi cơ sở dữ liệu
                songRepository.deleteById(songToDelete.getId());
                model.addAttribute("message", "Xóa bài hát thành công: " + fileName);
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi khi xóa bài hát: " + e.getMessage());
            System.out.println("Lỗi khi xóa: " + e.getMessage());
        }
    
        // Tải lại danh sách bài hát
        Pageable pageable = PageRequest.of(page, 5);
        Page<Song> songPage = songRepository.findAll(pageable);
        model.addAttribute("files", songPage.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        model.addAttribute("genres", GENRES);
        return "music";
    }

    @PostMapping("/search")
    public String searchSongs(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sourcePage", defaultValue = "music") String sourcePage,
            Model model) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Song> songPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            songPage = songRepository.findAll(pageable);
        } else {
            songPage = songRepository.searchByKeyword(keyword, pageable);
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        List<Playlist> playlists = playlistRepository.findAll();
        model.addAttribute("playlists", playlists);
        model.addAttribute("genres", GENRES);
        if ("recommendations".equals(sourcePage)) {
            model.addAttribute("recommendations", songPage.getContent());
            return "recommendations";
        } else {
            model.addAttribute("files", songPage.getContent());
            model.addAttribute("supabaseUrl", supabaseUrl);
            model.addAttribute("supabaseBucket", supabaseBucket);
            return "music";
        }
    }

    @PostMapping("/edit")
    public String editSong(
            @RequestParam("id") Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            Model model) throws IOException {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Song not found"));
        song.setTitle(title);
        song.setArtist(artist);
        song.setGenre(genre);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (song.getThumbnailUrl() != null) {
                String oldThumbnailName = song.getThumbnailUrl().substring(
                        song.getThumbnailUrl().lastIndexOf("/") + 1);
                supabaseStorageService.deleteFile(oldThumbnailName, "thumbnails");
            }
            String newThumbnailUrl = supabaseStorageService.uploadFile(thumbnail, "thumbnails");
            song.setThumbnailUrl(newThumbnailUrl);
        }
        songRepository.save(song);
        model.addAttribute("message", "Song updated successfully: " + title);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Song> songPage = songRepository.findAll(pageable);
        model.addAttribute("files", songPage.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        model.addAttribute("genres", GENRES);
        return "music";
    }

    @PostMapping("playlists/add-song")
    public String addSongToPlaylist(
            @RequestParam("songId") Long songId,
            @RequestParam("playlistId") Long playlistId,
            Model model) {
        try {
            // Kiểm tra bài hát tồn tại
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài hát với ID: " + songId));

            // Kiểm tra playlist tồn tại
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy playlist với ID: " + playlistId));

            // Kiểm tra xem bài hát đã có trong playlist chưa
            String checkQuery = "SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
            Long count = jdbcTemplate.queryForObject(checkQuery, new Object[]{playlistId, songId}, Long.class);
            if (count != null && count > 0) {
                model.addAttribute("message", "Bài hát đã có trong playlist: " + playlist.getName());
            } else {
                // Thêm bài hát vào playlist_songs
                String insertQuery = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
                jdbcTemplate.update(insertQuery, playlistId, songId);
                model.addAttribute("message", "Đã thêm bài hát " + song.getTitle() + " vào playlist " + playlist.getName());
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi khi thêm bài hát vào playlist: " + e.getMessage());
            System.out.println("Lỗi khi thêm bài hát: " + e.getMessage());
        }

        // Tải lại danh sách bài hát
        Pageable pageable = PageRequest.of(0, 5);
        Page<Song> songPage = songRepository.findAll(pageable);
        model.addAttribute("files", songPage.getContent());
        model.addAttribute("currentPage", songPage.getNumber());
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        model.addAttribute("genres", GENRES);
        model.addAttribute("playlists", playlistRepository.findAll());
        return "music";
    }
    /**
     * Hiển thị trang chi tiết bài hát
     * @param songId ID của bài hát
     * @param model Đối tượng Model để truyền dữ liệu vào view
     * @return Tên template (music_detail.html)
     */
    @GetMapping("/music/{songId}")
    public String getMusicDetail(@PathVariable("songId") Long songId, Model model) {
        Song song = songRepository.findById(songId).orElse(null);
        List<Playlist> playlists = playlistRepository.findAll();
        System.out.println("Song: " + (song != null ? song.toString() : "null"));
        System.out.println("Playlists: " + (playlists != null ? playlists.size() : "null"));
        System.out.println("Supabase URL: " + supabaseUrl);
        System.out.println("Supabase Bucket: " + supabaseBucket);
        model.addAttribute("song", song);
        model.addAttribute("playlists", playlists);
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseBucket", supabaseBucket);
        if (song == null) {
            model.addAttribute("message", "Không tìm thấy bài hát với ID: " + songId);
        }
        return "music_detail";
    }
}