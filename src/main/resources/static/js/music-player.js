document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const audio = document.createElement('audio');
    const audioSourceUrlEl = document.getElementById('audio-source-url');
    
    // Nếu không có URL audio, không cần thiết lập trình phát
    if (!audioSourceUrlEl) return;
    
    // Main player elements
    const playPauseBtn = document.getElementById('play-pause-btn');
    const prevBtn = document.getElementById('prev-btn');
    const nextBtn = document.getElementById('next-btn');
    const progressContainer = document.querySelector('.player-progress-container');
    const progressBar = document.querySelector('.player-progress-bar');
    const progressHandle = document.querySelector('.player-progress-handle');
    const currentTimeEl = document.querySelector('.player-current-time');
    const durationEl = document.querySelector('.player-duration');
    const volumeSlider = document.querySelector('.player-volume-slider');
    const volumeIcon = document.querySelector('.player-volume-icon');
    const visualizerContainer = document.querySelector('.player-visualizer');
    
    // Footer player elements
    const footerPrevBtn = document.getElementById('footer-prev-btn');
    const footerPlayBtn = document.getElementById('footer-play-btn');
    const footerNextBtn = document.getElementById('footer-next-btn');
    const footerTimeSlider = document.getElementById('footer-time-slider');
    const footerCurrentTime = document.getElementById('footer-current-time');
    const footerDuration = document.getElementById('footer-duration');
    const footerVolumeSlider = document.getElementById('footer-volume-slider');
    
    // Song information
    const currentSongEl = document.getElementById('current-song');
    const currentSongThumbnail = document.getElementById('current-song-thumbnail');
    
    // Initialize audio source
    const audioSourceUrl = audioSourceUrlEl.value;
    audio.src = audioSourceUrl;
    audio.preload = 'metadata';
    
    // State variables
    let isPlaying = false;
    let isSeeking = false;
    let lastVolume = 0.7;
    
    // Create visualizer bars
    const visualizerBarsCount = 64;
    const createVisualizer = () => {
        if (visualizerContainer) {
            visualizerContainer.innerHTML = '';
            for (let i = 0; i < visualizerBarsCount; i++) {
                const bar = document.createElement('div');
                bar.className = 'visualizer-bar';
                visualizerContainer.appendChild(bar);
            }
        }
    };
    createVisualizer();
    
    // Format time to MM:SS
    function formatTime(seconds) {
        if (isNaN(seconds) || !isFinite(seconds)) return '0:00';
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = Math.floor(seconds % 60);
        return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    }
    
    // Update progress UI
    function updateProgress() {
        if (!isSeeking && audio.duration) {
            const percent = (audio.currentTime / audio.duration) * 100;
            
            // Main player progress
            if (progressBar) progressBar.style.width = `${percent}%`;
            if (progressHandle) progressHandle.style.left = `${percent}%`;
            if (currentTimeEl) currentTimeEl.textContent = formatTime(audio.currentTime);
            
            // Footer progress
            if (footerTimeSlider) footerTimeSlider.value = percent;
            if (footerCurrentTime) footerCurrentTime.textContent = formatTime(audio.currentTime);
        }
    }
    
    // Update visualizer animation
    function updateVisualizer() {
        if (!isPlaying || !visualizerContainer) return;
        
        const bars = document.querySelectorAll('.visualizer-bar');
        bars.forEach((bar, index) => {
            // Create a pseudo-random height based on currentTime and index
            const seed = (audio.currentTime * 10 + index) % 15;
            const randomHeight = 5 + Math.sin(seed) * 15 + Math.cos(seed * 0.5) * 10;
            const height = Math.max(5, randomHeight);
            bar.style.height = `${height}px`;
        });
        
        requestAnimationFrame(updateVisualizer);
    }
    
    // Toggle play/pause functionality
    function togglePlay() {
        if (audio.paused) {
            audio.play()
                .then(() => {
                    isPlaying = true;
                    // Update main player button
                    if (playPauseBtn) {
                        playPauseBtn.innerHTML = '<i class="fas fa-pause text-2xl player-btn-icon"></i>';
                    }
                    // Update footer player button
                    if (footerPlayBtn) {
                        footerPlayBtn.innerHTML = '<i class="fas fa-pause"></i>';
                    }
                    updateVisualizer();
                })
                .catch(error => {
                    console.error('Error playing audio:', error);
                });
        } else {
            audio.pause();
            isPlaying = false;
            // Update main player button
            if (playPauseBtn) {
                playPauseBtn.innerHTML = '<i class="fas fa-play text-2xl player-btn-icon"></i>';
            }
            // Update footer player button
            if (footerPlayBtn) {
                footerPlayBtn.innerHTML = '<i class="fas fa-play"></i>';
            }
        }
    }
    
    // Set up progress tracking
    function setupProgressTracking() {
        if (progressContainer) {
            progressContainer.addEventListener('click', seek);
            
            progressContainer.addEventListener('mousedown', () => {
                isSeeking = true;
                audio.pause();
            });
            
            progressContainer.addEventListener('mousemove', (e) => {
                if (isSeeking) {
                    seek(e);
                }
            });
            
            // Touch support
            progressContainer.addEventListener('touchstart', (e) => {
                isSeeking = true;
                audio.pause();
            });
            
            progressContainer.addEventListener('touchmove', (e) => {
                if (isSeeking) {
                    e.preventDefault();
                    const touch = e.touches[0];
                    const seekEvent = {
                        clientX: touch.clientX,
                        target: progressContainer
                    };
                    seek(seekEvent);
                }
            });
        }
        
        // Footer time slider
        if (footerTimeSlider) {
            footerTimeSlider.addEventListener('input', () => {
                const seekTime = (footerTimeSlider.value / 100) * audio.duration;
                if (!isNaN(seekTime) && isFinite(seekTime)) {
                    audio.currentTime = seekTime;
                    if (currentTimeEl) currentTimeEl.textContent = formatTime(seekTime);
                    if (footerCurrentTime) footerCurrentTime.textContent = formatTime(seekTime);
                    if (progressBar) progressBar.style.width = `${footerTimeSlider.value}%`;
                    if (progressHandle) progressHandle.style.left = `${footerTimeSlider.value}%`;
                }
            });
        }
        
        document.addEventListener('mouseup', () => {
            if (isSeeking) {
                isSeeking = false;
                if (isPlaying) {
                    audio.play();
                }
            }
        });
        
        document.addEventListener('touchend', () => {
            if (isSeeking) {
                isSeeking = false;
                if (isPlaying) {
                    audio.play();
                }
            }
        });
    }
    
    // Seek functionality
    function seek(e) {
        if (!progressContainer) return;
        
        const rect = progressContainer.getBoundingClientRect();
        const pos = (e.clientX - rect.left) / rect.width;
        const seekTime = pos * audio.duration;
        
        if (!isNaN(seekTime) && isFinite(seekTime)) {
            audio.currentTime = seekTime;
            if (progressBar) progressBar.style.width = `${pos * 100}%`;
            if (progressHandle) progressHandle.style.left = `${pos * 100}%`;
            if (currentTimeEl) currentTimeEl.textContent = formatTime(seekTime);
            if (footerCurrentTime) footerCurrentTime.textContent = formatTime(seekTime);
            if (footerTimeSlider) footerTimeSlider.value = pos * 100;
        }
    }
    
    // Set up volume controls
    function setupVolumeControl() {
        // Set initial volume
        audio.volume = 0.7;
        if (volumeSlider) volumeSlider.value = audio.volume;
        if (footerVolumeSlider) footerVolumeSlider.value = audio.volume;
        
        // Main player volume slider
        if (volumeSlider) {
            volumeSlider.addEventListener('input', () => {
                audio.volume = volumeSlider.value;
                if (footerVolumeSlider) footerVolumeSlider.value = audio.volume;
                updateVolumeIcon();
                lastVolume = audio.volume;
            });
        }
        
        // Footer volume slider
        if (footerVolumeSlider) {
            footerVolumeSlider.addEventListener('input', () => {
                audio.volume = footerVolumeSlider.value;
                if (volumeSlider) volumeSlider.value = audio.volume;
                updateVolumeIcon();
                lastVolume = audio.volume;
            });
        }
        
        // Mute/unmute on volume icon click
        if (volumeIcon) {
            volumeIcon.addEventListener('click', toggleMute);
        }
    }
    
    // Toggle mute function
    function toggleMute() {
        if (audio.volume > 0) {
            lastVolume = audio.volume;
            audio.volume = 0;
            if (volumeSlider) volumeSlider.value = 0;
            if (footerVolumeSlider) footerVolumeSlider.value = 0;
        } else {
            audio.volume = lastVolume;
            if (volumeSlider) volumeSlider.value = lastVolume;
            if (footerVolumeSlider) footerVolumeSlider.value = lastVolume;
        }
        updateVolumeIcon();
    }
    
    // Update volume icon based on current volume
    function updateVolumeIcon() {
        if (!volumeIcon) return;
        
        if (audio.volume > 0.5) {
            volumeIcon.innerHTML = '<i class="fas fa-volume-up"></i>';
        } else if (audio.volume > 0) {
            volumeIcon.innerHTML = '<i class="fas fa-volume-down"></i>';
        } else {
            volumeIcon.innerHTML = '<i class="fas fa-volume-mute"></i>';
        }
    }
    
    // Add event listeners to main player buttons
    if (playPauseBtn) playPauseBtn.addEventListener('click', togglePlay);
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            audio.currentTime = 0;
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (audio.duration) {
                audio.currentTime = Math.max(0, audio.duration - 5);
            }
        });
    }
    
    // Add event listeners to footer player buttons
    if (footerPlayBtn) footerPlayBtn.addEventListener('click', togglePlay);
    if (footerPrevBtn) {
        footerPrevBtn.addEventListener('click', () => {
            audio.currentTime = 0;
        });
    }
    if (footerNextBtn) {
        footerNextBtn.addEventListener('click', () => {
            if (audio.duration) {
                audio.currentTime = Math.max(0, audio.duration - 5);
            }
        });
    }
    
    // Audio events
    audio.addEventListener('timeupdate', updateProgress);
    
    audio.addEventListener('loadedmetadata', () => {
        if (durationEl) durationEl.textContent = formatTime(audio.duration);
        if (currentTimeEl) currentTimeEl.textContent = '0:00';
        if (footerDuration) footerDuration.textContent = formatTime(audio.duration);
        if (footerCurrentTime) footerCurrentTime.textContent = '0:00';
    });
    
    audio.addEventListener('ended', () => {
        isPlaying = false;
        if (playPauseBtn) playPauseBtn.innerHTML = '<i class="fas fa-play text-2xl player-btn-icon"></i>';
        if (footerPlayBtn) footerPlayBtn.innerHTML = '<i class="fas fa-play"></i>';
    });
    
    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (e.code === 'Space') {
            e.preventDefault();
            togglePlay();
        } else if (e.code === 'ArrowLeft') {
            audio.currentTime = Math.max(0, audio.currentTime - 5);
        } else if (e.code === 'ArrowRight') {
            audio.currentTime = Math.min(audio.duration, audio.currentTime + 5);
        } else if (e.code === 'ArrowUp') {
            audio.volume = Math.min(1, audio.volume + 0.1);
            if (volumeSlider) volumeSlider.value = audio.volume;
            if (footerVolumeSlider) footerVolumeSlider.value = audio.volume;
            updateVolumeIcon();
        } else if (e.code === 'ArrowDown') {
            audio.volume = Math.max(0, audio.volume - 0.1);
            if (volumeSlider) volumeSlider.value = audio.volume;
            if (footerVolumeSlider) footerVolumeSlider.value = audio.volume;
            updateVolumeIcon();
        } else if (e.code === 'KeyM') {
            toggleMute();
        }
    });
    
    // Initialize player
    setupProgressTracking();
    setupVolumeControl();
    updateVolumeIcon();
    
    // Add animation to album art
    const albumArt = document.querySelector('.aspect-square img');
    if (albumArt) {
        audio.addEventListener('play', () => {
            albumArt.classList.add('scale-105');
        });
        
        audio.addEventListener('pause', () => {
            albumArt.classList.remove('scale-105');
        });
    }
    
    // Update song information in footer player
    if (currentSongEl && document.querySelector('.text-4xl')) {
        currentSongEl.textContent = document.querySelector('.text-4xl').textContent.trim();
    }
});