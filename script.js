function toggleUploadForm() {
    document.getElementById('uploadForm').classList.toggle('hidden');
}

function toggleEditForm(id) {
    const form = document.getElementById('editForm' + id);
    form.classList.toggle('active');
}

// Đồng bộ thanh phát nhạc
const playerAudio = document.getElementById('player-audio');
const playPauseBtn = document.getElementById('play-pause-btn');
const currentSong = document.getElementById('current-song');
const timeSlider = document.getElementById('time-slider');
const currentTime = document.getElementById('current-time');
const duration = document.getElementById('duration');
const volumeSlider = document.getElementById('volume-slider');
let currentAudio = null;

document.querySelectorAll('.song-audio').forEach(audio => {
    audio.addEventListener('play', function() {
        if (currentAudio && currentAudio !== audio) {
            currentAudio.pause();
        }
        currentAudio = audio;
        playerAudio.src = audio.querySelector('source').src;
        playerAudio.play();
        updatePlayerInfo(audio);
    });

    audio.addEventListener('pause', function() {
        if (currentAudio === audio) {
            playerAudio.pause();
            playPauseBtn.textContent = '▶️';
        }
    });
});

playPauseBtn.addEventListener('click', () => {
    if (playerAudio.paused && currentAudio) {
        playerAudio.play();
        currentAudio.play();
        playPauseBtn.textContent = '⏸';
    } else if (!playerAudio.paused) {
        playerAudio.pause();
        currentAudio.pause();
        playPauseBtn.textContent = '▶️';
    }
});

playerAudio.addEventListener('timeupdate', () => {
    const current = playerAudio.currentTime;
    const total = playerAudio.duration;
    timeSlider.value = (current / total) * 100 || 0;
    currentTime.textContent = formatTime(current);
    duration.textContent = formatTime(total);
});

timeSlider.addEventListener('input', () => {
    const time = (timeSlider.value / 100) * playerAudio.duration;
    playerAudio.currentTime = time;
    currentAudio.currentTime = time;
});

volumeSlider.addEventListener('input', () => {
    playerAudio.volume = volumeSlider.value;
    if (currentAudio) currentAudio.volume = volumeSlider.value;
});

function updatePlayerInfo(audio) {
    const title = audio.getAttribute('data-title');
    const artist = audio.getAttribute('data-artist') || 'Không rõ';
    currentSong.textContent = `${title} - ${artist}`;
    playPauseBtn.textContent = '⏸';
    duration.textContent = formatTime(audio.duration);
}

function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' + secs : secs}`;
}