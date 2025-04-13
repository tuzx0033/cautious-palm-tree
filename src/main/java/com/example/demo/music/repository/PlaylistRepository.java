package com.example.demo.music.repository;

import com.example.demo.music.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.id = :id")
    Optional<Playlist> findByIdWithSongs(@Param("id") Long id);
    
}