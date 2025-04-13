package com.example.demo.music.controller;

import com.example.demo.music.entity.Playlist;
import com.example.demo.music.entity.Song;
import com.example.demo.music.repository.PlaylistRepository;
import com.example.demo.music.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @GetMapping
    public String showRecommendations(Model model, HttpServletResponse response) {
        // Tắt cache để đảm bảo danh sách luôn mới
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Pageable pageable = PageRequest.of(0, 5);
        List<Song> recommendations = songRepository.findRandomSongs(pageable);
        List<Playlist> playlists = playlistRepository.findAll();

        model.addAttribute("recommendations", recommendations);
        model.addAttribute("playlists", playlists);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1); // Gợi ý không phân trang

        return "recommendations";
    }

    @PostMapping("/search")
    public String searchRecommendations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            HttpServletResponse response) {
        // Tắt cache
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Pageable pageable = PageRequest.of(page, 5);
        List<Song> recommendations;
        int totalPages;

        if (keyword == null || keyword.trim().isEmpty()) {
            recommendations = songRepository.findRandomSongs(pageable);
            totalPages = 1; // Không phân trang cho gợi ý ngẫu nhiên
        } else {
            recommendations = songRepository.searchByKeyword(keyword, pageable).getContent();
            totalPages = songRepository.searchByKeyword(keyword, pageable).getTotalPages();
        }

        List<Playlist> playlists = playlistRepository.findAll();

        model.addAttribute("recommendations", recommendations);
        model.addAttribute("playlists", playlists);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "recommendations";
    }
}