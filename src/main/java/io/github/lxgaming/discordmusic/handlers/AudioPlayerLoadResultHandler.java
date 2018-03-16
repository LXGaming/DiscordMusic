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

package io.github.lxgaming.discordmusic.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.util.DiscordData;

public final class AudioPlayerLoadResultHandler implements AudioLoadResultHandler {
    
    private final DiscordData discordData;
    
    public AudioPlayerLoadResultHandler(DiscordData discordData) {
        this.discordData = discordData;
    }
    
    @Override
    public void trackLoaded(AudioTrack track) {
        AudioManager.track(getDiscordData(), track);
    }
    
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioManager.playlist(getDiscordData(), playlist);
    }
    
    @Override
    public void noMatches() {
        AudioManager.exception(getDiscordData(), new FriendlyException("No matches found", FriendlyException.Severity.COMMON, null));
    }
    
    @Override
    public void loadFailed(FriendlyException exception) {
        AudioManager.exception(getDiscordData(), exception);
    }
    
    private DiscordData getDiscordData() {
        return discordData;
    }
}