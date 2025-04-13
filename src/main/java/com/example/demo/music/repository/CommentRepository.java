package com.example.demo.music.repository;

import com.example.demo.music.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findBySongId(Long songId, Pageable pageable);
    Page<Comment> findByPlaylistId(Long playlistId, Pageable pageable);
    List<Comment> findBySongId(Long songId);
    List<Comment> findByPlaylistId(Long playlistId);

    // Thêm truy vấn tùy chỉnh để sắp xếp bình luận mới nhất trước
    @Query("SELECT c FROM Comment c WHERE c.song.id = :songId ORDER BY c.createdAt DESC")
    Page<Comment> findBySongIdOrdered(@Param("songId") Long songId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.playlist.id = :playlistId ORDER BY c.createdAt DESC")
    Page<Comment> findByPlaylistIdOrdered(@Param("playlistId") Long playlistId, Pageable pageable);
}