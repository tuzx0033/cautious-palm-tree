package com.example.demo.music.repository;

import com.example.demo.music.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT s FROM Song s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Song> searchByKeyword(String keyword, Pageable pageable);

    Page<Song> findAll(Pageable pageable);

    @Query("SELECT s FROM Song s ORDER BY RANDOM()")
    List<Song> findRandomSongs(Pageable pageable);

    Page<Song> findByGenre(String genre, Pageable pageable);

    // Tìm bài hát theo fileUrl chứa fileName
    Optional<Song> findByFileUrlContaining(String fileName);
}