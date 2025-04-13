console.log('Loaded custom script.js v3');

function toggleUploadForm() {
    const uploadSection = document.getElementById('uploadForm');
    if (uploadSection) {
        uploadSection.classList.toggle('active');
        console.log('Toggled upload form:', uploadSection.classList.contains('active') ? 'Shown' : 'Hidden');
    } else {
        console.error('Upload form not found');
    }
}

function toggleEditForm(id) {
    const form = document.getElementById('editForm' + id);
    form.classList.toggle('active');
}

// Đồng bộ thanh phát nhạc
const playerAudio = document.getElementById('player-audio');
const playPauseBtn = document.getElementById('play-pause-btn');
const currentSong = document.getElementById('current-song');
const currentThumbnail = document.getElementById('current-song-thumbnail');
const timeSlider = document.getElementById('time-slider');
const currentTime = document.getElementById('current-time');
const duration = document.getElementById('duration');
const volumeSlider = document.getElementById('volume-slider');
let currentAudio = null;

// Khởi tạo trạng thái ban đầu
if (playPauseBtn) {
    const icon = playPauseBtn.querySelector('i');
    icon.classList.remove('fa-pause');
    icon.classList.add('fa-play');
    playPauseBtn.textContent = '';
    playPauseBtn.appendChild(icon);
    console.log('Khởi tạo nút play/pause: fa-play');
}

document.querySelectorAll('.song-audio').forEach(audio => {
    audio.addEventListener('play', function() {
        console.log('Phát bài hát:', audio.getAttribute('data-title'));
        if (currentAudio && currentAudio !== audio) {
            currentAudio.pause();
        }
        currentAudio = audio;
        playerAudio.src = audio.querySelector('source').src;
        playerAudio.play();
        updatePlayerInfo(audio);
        const icon = playPauseBtn.querySelector('i');
        icon.classList.remove('fa-play');
        icon.classList.add('fa-pause');
        playPauseBtn.textContent = '';
        playPauseBtn.appendChild(icon);
        console.log('Chuyển nút thành fa-pause');
    });

    audio.addEventListener('pause', function() {
        if (currentAudio === audio) {
            playerAudio.pause();
            const icon = playPauseBtn.querySelector('i');
            icon.classList.remove('fa-pause');
            icon.classList.add('fa-play');
            playPauseBtn.textContent = '';
            playPauseBtn.appendChild(icon);
            console.log('Chuyển nút thành fa-play (pause)');
        }
    });
});

if (playPauseBtn) {
    playPauseBtn.addEventListener('click', () => {
        if (playerAudio.paused && currentAudio) {
            playerAudio.play();
            currentAudio.play();
            const icon = playPauseBtn.querySelector('i');
            icon.classList.remove('fa-play');
            icon.classList.add('fa-pause');
            playPauseBtn.textContent = '';
            playPauseBtn.appendChild(icon);
            console.log('Click: Chuyển nút thành fa-pause');
        } else if (!playerAudio.paused) {
            playerAudio.pause();
            currentAudio.pause();
            const icon = playPauseBtn.querySelector('i');
            icon.classList.remove('fa-pause');
            icon.classList.add('fa-play');
            playPauseBtn.textContent = '';
            playPauseBtn.appendChild(icon);
            console.log('Click: Chuyển nút thành fa-play');
        }
    });
}

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
    if (currentAudio) currentAudio.currentTime = time;
});

volumeSlider.addEventListener('input', () => {
    playerAudio.volume = volumeSlider.value;
    if (currentAudio) currentAudio.volume = volumeSlider.value;
});

function updatePlayerInfo(audio) {
    const title = audio.getAttribute('data-title');
    const artist = audio.getAttribute('data-artist') || 'Không rõ';
    const thumbnail = audio.parentElement.parentElement.querySelector('img')?.src;
    currentSong.textContent = `${title} - ${artist}`;
    
    // Cập nhật thumbnail
    if (thumbnail) {
        currentThumbnail.src = thumbnail;
        currentThumbnail.classList.remove('hidden');
    } else {
        currentThumbnail.classList.add('hidden');
    }
    
    const icon = playPauseBtn.querySelector('i');
    icon.classList.remove('fa-play');
    icon.classList.add('fa-pause');
    playPauseBtn.textContent = '';
    playPauseBtn.appendChild(icon);
    duration.textContent = formatTime(audio.duration);
    console.log('updatePlayerInfo: Chuyển nút thành fa-pause, thumbnail:', thumbnail || 'none');
}

function formatTime(seconds) {
    if (isNaN(seconds)) return '0:00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' + secs : secs}`;
}
function toggleCommentSection(songId) {
    const section = document.getElementById(`comment-section-${songId}`);
    section.classList.toggle('hidden');
    if (!section.classList.contains('hidden')) {
        loadComments('song', songId);
    }
}

function loadComments(type, id) {
    const url = type === 'song' 
        ? `/api/comments/song/${id}` 
        : `/api/comments/playlist/${id}`;
    fetch(url)
        .then(response => response.text())
        .then(html => {
            document.getElementById(`comments-${id}`).innerHTML = html;
        })
        .catch(error => {
            console.error('Error loading comments:', error);
            document.getElementById(`comments-${id}`).innerHTML = '<p class="text-red-500 text-sm">Lỗi khi tải bình luận.</p>';
        });
}

function submitComment(event) {
    event.preventDefault();
    // Gửi yêu cầu AJAX
    fetch('/api/comments', { method: 'POST' })
        .then(response => response.json())
        .catch(error => {
            // Lỗi tại dòng 191
            document.getElementById('comment-error').innerHTML = 'Lỗi khi gửi bình luận';
        });
}