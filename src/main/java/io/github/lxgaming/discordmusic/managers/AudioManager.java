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

package io.github.lxgaming.discordmusic.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.config.Account;
import io.github.lxgaming.discordmusic.handlers.AudioPlayerSendHandler;
import io.github.lxgaming.discordmusic.listeners.AudioListener;
import io.github.lxgaming.discordmusic.util.Color;
import io.github.lxgaming.discordmusic.util.DiscordData;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audio.SpeakingMode;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class AudioManager {
    
    private static final AudioPlayerManager AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static final Map<Long, AudioPlayer> AUDIO_PLAYERS = Collections.synchronizedMap(Toolbox.newHashMap());
    private static final Map<Long, BlockingQueue<AudioTrack>> AUDIO_QUEUES = Collections.synchronizedMap(Toolbox.newHashMap());
    private static final Map<Long, Map<DiscordData, List<AudioTrack>>> SEARCH_RESULTS = Collections.synchronizedMap(Toolbox.newHashMap());
    
    public static void registerAudioPlayer(Guild guild) {
        if (getAudioPlayers().containsKey(guild.getIdLong()) || getAudioQueues().containsKey(guild.getIdLong()) || getSearchResults().containsKey(guild.getIdLong())) {
            DiscordMusic.getInstance().getLogger().warn("{} ({}) AudioPlayer is already registered", guild.getName(), guild.getIdLong());
            return;
        }
        
        if (!(guild.getAudioManager().getSendingHandler() instanceof AudioPlayerSendHandler)) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(guild));
        }
        
        guild.getAudioManager().setSpeakingMode(AccountManager.getAccount().map(Account::getSpeakingMode).orElse(SpeakingMode.VOICE));
        
        AudioPlayer audioPlayer = getAudioPlayerManager().createPlayer();
        audioPlayer.addListener(new AudioListener());
        audioPlayer.setVolume(Math.min(DiscordMusic.getInstance().getConfig().map(Config::getMaxVolume).orElse(150), DiscordMusic.getInstance().getConfig().map(Config::getDefaultVolume).orElse(50)));
        getAudioPlayers().put(guild.getIdLong(), audioPlayer);
        getAudioQueues().put(guild.getIdLong(), Toolbox.newLinkedBlockingQueue());
        getSearchResults().put(guild.getIdLong(), Toolbox.newHashMap());
    }
    
    public static void unregisterAudioPlayer(Guild guild) {
        getAudioPlayers().remove(guild.getIdLong());
        getAudioQueues().remove(guild.getIdLong());
        getSearchResults().remove(guild.getIdLong());
    }
    
    public static boolean pause(Guild guild) {
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(guild);
        audioPlayer.setPaused(true);
        return true;
    }
    
    public static boolean play(Guild guild) {
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(guild);
        audioPlayer.setPaused(false);
        if (audioPlayer.getPlayingTrack() == null) {
            return playNext(audioPlayer, getAudioQueue(guild).poll());
        }
        
        return true;
    }
    
    public static boolean playNext(DiscordData discordData) {
        return discordData != null && discordData.isValid() && playNext(discordData.getGuild());
    }
    
    public static boolean playNext(Guild guild) {
        return playNext(AudioManager.getAudioPlayer(guild), getAudioQueue(guild).poll());
    }
    
    public static boolean playNext(AudioPlayer audioPlayer, AudioTrack audioTrack) {
        if (audioTrack == null) {
            audioPlayer.playTrack(null);
            return false;
        }
        
        DiscordData discordData = audioTrack.getUserData(DiscordData.class);
        if (discordData == null || !discordData.isValid()) {
            return false;
        }
        
        audioPlayer.playTrack(audioTrack);
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        embedBuilder.setTitle("Now playing");
        embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
        MessageManager.sendTemporaryMessage(discordData.getMessage().getChannel(), embedBuilder.build());
        return true;
    }
    
    public static boolean canPlayNext(Guild guild) {
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(guild);
        return (audioPlayer.getPlayingTrack() == null || audioPlayer.getPlayingTrack().getInfo().isStream) && guild.getAudioManager().isConnected();
    }
    
    public static void exception(DiscordData discordData, FriendlyException exception) {
        if (!discordData.isValid()) {
            return;
        }
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
        embedBuilder.setTitle("Encountered an error");
        embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(exception.getMessage(), "Unknown"));
        embedBuilder.setFooter("Details available in console", null);
        MessageManager.sendTemporaryMessage(discordData.getMessage().getChannel(), embedBuilder.build());
    }
    
    public static void playlist(DiscordData discordData, AudioPlaylist audioPlaylist) {
        try {
            if (!discordData.isValid()) {
                throw new InvalidParameterException("DiscordData cannot be invalid");
            }
            
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
            if (audioPlaylist.isSearchResult()) {
                List<AudioTrack> audioTracks = Toolbox.newArrayList();
                for (int index = 0; index < Math.min(audioPlaylist.getTracks().size(), 5); index++) {
                    AudioTrack audioTrack = audioPlaylist.getTracks().get(index);
                    audioTrack.setUserData(discordData);
                    audioTracks.add(audioTrack);
                    if (embedBuilder.getDescriptionBuilder().length() != 0) {
                        embedBuilder.getDescriptionBuilder().append("\n");
                    }
                    
                    embedBuilder.getDescriptionBuilder().append("**").append(index + 1).append(".** ");
                    embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
                }
                
                //TODO Need to clear SearchResults after a certain amount of time has passed.
                
                getSearchResults().get(discordData.getGuild().getIdLong()).put(discordData, audioTracks);
                embedBuilder.setTitle("Search results");
                embedBuilder.setFooter("Use " + DiscordMusic.getInstance().getConfig().map(Config::getCommandPrefix).orElse("/") + "Select [ID...]", null);
                MessageManager.sendTemporaryMessage(discordData.getMessage().getChannel(), embedBuilder.build());
                return;
            }
            
            if (audioPlaylist.getSelectedTrack() != null) {
                track(discordData, audioPlaylist.getSelectedTrack());
                return;
            }
            
            for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                audioTrack.setUserData(discordData);
                getAudioQueue(discordData.getGuild()).offer(audioTrack);
            }
            
            if (canPlayNext(discordData.getGuild())) {
                playNext(discordData.getGuild());
            }
            
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle(audioPlaylist.getTracks().size() + " tracks added to queue");
            MessageManager.sendTemporaryMessage(discordData.getMessage().getChannel(), embedBuilder.build());
        } catch (RuntimeException ex) {
            exception(discordData, new FriendlyException(ex.getMessage(), FriendlyException.Severity.COMMON, ex));
        }
    }
    
    public static void track(DiscordData discordData, AudioTrack audioTrack) {
        try {
            if (!discordData.isValid()) {
                throw new InvalidParameterException("DiscordData cannot be invalid");
            }
            
            audioTrack.setUserData(discordData);
            getAudioQueue(discordData.getGuild()).offer(audioTrack);
            if (canPlayNext(discordData.getGuild())) {
                playNext(discordData.getGuild());
                return;
            }
            
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Track added to queue");
            embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
            MessageManager.sendTemporaryMessage(discordData.getMessage().getChannel(), embedBuilder.build());
        } catch (RuntimeException ex) {
            exception(discordData, new FriendlyException(ex.getMessage(), FriendlyException.Severity.COMMON, ex));
        }
    }
    
    public static AudioPlayer getAudioPlayer(Guild guild) {
        return getAudioPlayers().get(guild.getIdLong());
    }
    
    public static BlockingQueue<AudioTrack> getAudioQueue(Guild guild) {
        return getAudioQueues().get(guild.getIdLong());
    }
    
    public static void removeSearchResult(Member member) {
        Map.Entry<DiscordData, List<AudioTrack>> entry = getSearchResult(member);
        if (entry == null) {
            return;
        }
        
        getSearchResults().get(member.getGuild().getIdLong()).remove(entry.getKey());
    }
    
    public static Map.Entry<DiscordData, List<AudioTrack>> getSearchResult(Member member) {
        Map<DiscordData, List<AudioTrack>> searchResults = getSearchResults().get(member.getGuild().getIdLong());
        if (searchResults == null) {
            return null;
        }
        
        for (Map.Entry<DiscordData, List<AudioTrack>> entry : searchResults.entrySet()) {
            if (entry.getKey().isValid() && entry.getKey().getMessage().getAuthor().getIdLong() == member.getUser().getIdLong()) {
                return entry;
            }
        }
        
        return null;
    }
    
    public static AudioPlayerManager getAudioPlayerManager() {
        return AUDIO_PLAYER_MANAGER;
    }
    
    private static Map<Long, AudioPlayer> getAudioPlayers() {
        return AUDIO_PLAYERS;
    }
    
    private static Map<Long, BlockingQueue<AudioTrack>> getAudioQueues() {
        return AUDIO_QUEUES;
    }
    
    private static Map<Long, Map<DiscordData, List<AudioTrack>>> getSearchResults() {
        return SEARCH_RESULTS;
    }
}