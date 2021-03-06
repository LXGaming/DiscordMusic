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

package io.github.lxgaming.discordmusic.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.entity.AudioTrackData;
import io.github.lxgaming.discordmusic.manager.AudioManager;

public class AudioListener extends AudioEventAdapter {
    
    @Override
    public void onPlayerPause(AudioPlayer player) {
        DiscordMusic.getInstance().getLogger().debug("Track pause");
    }
    
    @Override
    public void onPlayerResume(AudioPlayer player) {
        DiscordMusic.getInstance().getLogger().debug("Track resume");
    }
    
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        DiscordMusic.getInstance().getLogger().debug("Track start - {} ({})", track.getInfo().title, track.getInfo().uri);
    }
    
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        DiscordMusic.getInstance().getLogger().debug("Track end - {} - {} ({})", endReason.name(), track.getInfo().title, track.getInfo().uri);
        if (endReason.mayStartNext) {
            AudioManager.getData(track).map(AudioTrackData::getGuild).ifPresent(AudioManager::playNext);
        }
    }
    
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        DiscordMusic.getInstance().getLogger().debug("Track exception - {} - {} ({})", exception.getMessage(), track.getInfo().title, track.getInfo().uri);
        AudioManager.getData(track).map(AudioTrackData::getChannel).ifPresent(channel -> AudioManager.exception(channel, exception));
    }
    
    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        DiscordMusic.getInstance().getLogger().debug("Track stuck - {} ({})", track.getInfo().title, track.getInfo().uri);
        AudioManager.getData(track).map(AudioTrackData::getChannel).ifPresent(channel -> {
            AudioManager.exception(channel, new FriendlyException("Track stuck", FriendlyException.Severity.COMMON, null));
        });
    }
}