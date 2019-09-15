/*
 * Copyright 2018 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.discordmusic.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.data.AudioTrackData;
import io.github.lxgaming.discordmusic.manager.AudioManager;

public final class AudioPlayerLoadResultHandler implements AudioLoadResultHandler {
    
    private final AudioTrackData trackData;
    
    public AudioPlayerLoadResultHandler(AudioTrackData trackData) {
        this.trackData = trackData;
    }
    
    @Override
    public void trackLoaded(AudioTrack track) {
        track.setUserData(trackData);
        AudioManager.track(track);
    }
    
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        playlist.getTracks().forEach(track -> track.setUserData(trackData));
        if (playlist.getSelectedTrack() != null) {
            playlist.getSelectedTrack().setUserData(trackData);
        }
        
        AudioManager.playlist(playlist);
    }
    
    @Override
    public void noMatches() {
        loadFailed(new FriendlyException("No matches found", FriendlyException.Severity.COMMON, null));
    }
    
    @Override
    public void loadFailed(FriendlyException exception) {
        AudioManager.exception(trackData.getChannel(), exception);
    }
}