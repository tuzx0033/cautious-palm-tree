package com.example.demo.music.controller;

import com.example.demo.music.entity.Comment;
import com.example.demo.music.entity.Playlist;
import com.example.demo.music.entity.Song;
import com.example.demo.music.repository.CommentRepository;
import com.example.demo.music.repository.PlaylistRepository;
import com.example.demo.music.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    // Lấy bình luận cho bài hát
    @GetMapping("/song/{songId}")
    public String getSongComments(
            @PathVariable("songId") Long songId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findBySongIdOrdered(songId, pageable);
        model.addAttribute("comments", comments.getContent());
        model.addAttribute("songId", songId);
        model.addAttribute("currentPage", comments.getNumber());
        model.addAttribute("totalPages", comments.getTotalPages());
        return "fragments/comments :: commentList";
    }

    // Lấy bình luận cho playlist
    @GetMapping("/playlist/{playlistId}")
    public String getPlaylistComments(
            @PathVariable("playlistId") Long playlistId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPlaylistIdOrdered(playlistId, pageable);
        model.addAttribute("comments", comments.getContent());
        model.addAttribute("playlistId", playlistId);
        model.addAttribute("currentPage", comments.getNumber());
        model.addAttribute("totalPages", comments.getTotalPages());
        return "fragments/comments :: commentList";
    }

    // Thêm bình luận
    @PostMapping
    public String addComment(
            @RequestParam(value = "songId", required = false) Long songId,
            @RequestParam(value = "playlistId", required = false) Long playlistId,
            @RequestParam("content") String content,
            Model model) {
        if (content == null || content.trim().isEmpty() || content.length() > 500) {
            model.addAttribute("message", "Bình luận không hợp lệ.");
        } else {
            Comment comment = new Comment(content, null, null);
            if (songId != null) {
                Song song = songRepository.findById(songId)
                        .orElseThrow(() -> new IllegalArgumentException("Song not found"));
                comment.setSong(song);
            } else if (playlistId != null) {
                Playlist playlist = playlistRepository.findById(playlistId)
                        .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
                comment.setPlaylist(playlist);
            } else {
                model.addAttribute("message", "Phải chọn bài hát hoặc playlist.");
                return "fragments/comments :: commentList";
            }
            commentRepository.save(comment);
            model.addAttribute("message", "Bình luận đã được gửi.");
        }

        // Tải lại bình luận
        Pageable pageable = PageRequest.of(0, 10);
        if (songId != null) {
            Page<Comment> comments = commentRepository.findBySongIdOrdered(songId, pageable);
            model.addAttribute("comments", comments.getContent());
            model.addAttribute("songId", songId);
            model.addAttribute("currentPage", comments.getNumber());
            model.addAttribute("totalPages", comments.getTotalPages());
        } else {
            Page<Comment> comments = commentRepository.findByPlaylistIdOrdered(playlistId, pageable);
            model.addAttribute("comments", comments.getContent());
            model.addAttribute("playlistId", playlistId);
            model.addAttribute("currentPage", comments.getNumber());
            model.addAttribute("totalPages", comments.getTotalPages());
        }
        return "fragments/comments :: commentList";
    }
}