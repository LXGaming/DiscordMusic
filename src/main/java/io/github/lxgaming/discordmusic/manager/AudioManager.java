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

package io.github.lxgaming.discordmusic.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.configuration.category.GuildCategory;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.entity.AudioTrackData;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.handler.AudioPlayerSendHandler;
import io.github.lxgaming.discordmusic.listener.AudioListener;
import io.github.lxgaming.discordmusic.menu.ReactionMenu;
import io.github.lxgaming.discordmusic.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class AudioManager {
    
    public static final AudioPlayerManager AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static final AudioListener AUDIO_LISTENER = new AudioListener();
    private static final Map<Long, AudioPlayer> AUDIO_PLAYERS = Maps.newConcurrentMap();
    private static final Map<Long, BlockingQueue<AudioTrack>> AUDIO_QUEUES = Maps.newConcurrentMap();
    private static final Map<Long, Map<AudioTrackData, List<AudioTrack>>> SEARCH_RESULTS = Maps.newConcurrentMap();
    
    public static void prepare() {
        GeneralCategory generalCategory = DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).orElseThrow(NullPointerException::new);
        if (generalCategory.getMaxVolume() < 0) {
            DiscordMusic.getInstance().getLogger().warn("MaxVolume is out of bounds. Resetting to {}", GeneralCategory.DEFAULT_MAX_VOLUME);
            // Discord limit
            generalCategory.setMaxVolume(GeneralCategory.DEFAULT_MAX_VOLUME);
        } else if (generalCategory.getMaxVolume() > 1000) {
            DiscordMusic.getInstance().getLogger().warn("MaxVolume is out of bounds. Resetting to {}", 1000);
            // LavaPlayer limit - DefaultAudioPlayer::setVolume
            generalCategory.setMaxVolume(1000);
        }
        
        if (generalCategory.getDefaultVolume() < 0 || generalCategory.getDefaultVolume() > generalCategory.getMaxVolume()) {
            DiscordMusic.getInstance().getLogger().warn("DefaultVolume is out of bounds. Resetting to {}", GeneralCategory.DEFAULT_VOLUME);
            generalCategory.setDefaultVolume(GeneralCategory.DEFAULT_VOLUME);
        }
        
        if (generalCategory.getSearchLimit() < 0 || generalCategory.getSearchLimit() > 10) {
            DiscordMusic.getInstance().getLogger().warn("SearchLimit is out of bounds. Resetting to {}", GeneralCategory.DEFAULT_SEARCH_LIMIT);
            generalCategory.setDefaultVolume(GeneralCategory.DEFAULT_SEARCH_LIMIT);
        }
        
        if (generalCategory.getTrackStuckThreshold() <= 0) {
            DiscordMusic.getInstance().getLogger().warn("TrackStuckThreshold is out of bounds. Resetting to {}", GeneralCategory.DEFAULT_TRACK_STUCK_THRESHOLD);
            generalCategory.setTrackStuckThreshold(GeneralCategory.DEFAULT_TRACK_STUCK_THRESHOLD);
        }
        
        AUDIO_PLAYER_MANAGER.setTrackStuckThreshold(generalCategory.getTrackStuckThreshold());
    }
    
    public static void register(Guild guild) {
        if (AUDIO_PLAYERS.containsKey(guild.getIdLong())) {
            DiscordMusic.getInstance().getLogger().warn("{} ({}) AudioPlayer is already registered", guild.getName(), guild.getIdLong());
            return;
        }
        
        AudioPlayer audioPlayer = AUDIO_PLAYER_MANAGER.createPlayer();
        audioPlayer.addListener(AUDIO_LISTENER);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(audioPlayer));
        
        Config config = DiscordMusic.getInstance().getConfig().orElse(null);
        if (config != null) {
            audioPlayer.setVolume(Math.min(config.getGeneralCategory().getDefaultVolume(), config.getGeneralCategory().getMaxVolume()));
            guild.getAudioManager().setSpeakingMode(config.getAccountCategory().getSpeakingMode());
        } else {
            audioPlayer.setVolume(GeneralCategory.DEFAULT_VOLUME);
            guild.getAudioManager().setSpeakingMode(SpeakingMode.VOICE);
        }
        
        GuildCategory guildCategory = DiscordManager.getGuildCategory(guild);
        if (guildCategory != null && guildCategory.getAutoJoinChannel() != 0L) {
            VoiceChannel voiceChannel = guild.getVoiceChannelById(guildCategory.getAutoJoinChannel());
            if (voiceChannel != null) {
                try {
                    guild.getAudioManager().openAudioConnection(voiceChannel);
                } catch (Exception ex) {
                    DiscordMusic.getInstance().getLogger().error("Encountered an error while connecting to {} ({}): {}", voiceChannel.getName(), voiceChannel.getIdLong(), ex.getMessage());
                }
            } else {
                DiscordMusic.getInstance().getLogger().warn("AutoJoinChannel does not exist");
                guildCategory.setAutoJoinChannel(0L);
            }
        }
        
        AUDIO_PLAYERS.put(guild.getIdLong(), audioPlayer);
        AUDIO_QUEUES.put(guild.getIdLong(), Queues.newLinkedBlockingQueue());
        SEARCH_RESULTS.put(guild.getIdLong(), Maps.newConcurrentMap());
    }
    
    public static void unregister(Guild guild) {
        AUDIO_PLAYERS.remove(guild.getIdLong());
        AUDIO_QUEUES.remove(guild.getIdLong());
        SEARCH_RESULTS.remove(guild.getIdLong());
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
    
    public static boolean playNext(Guild guild) {
        return playNext(AudioManager.getAudioPlayer(guild), getAudioQueue(guild).poll());
    }
    
    public static boolean playNext(AudioPlayer audioPlayer, AudioTrack audioTrack) {
        if (audioTrack == null) {
            audioPlayer.playTrack(null);
            return false;
        }
        
        AudioTrackData trackData = getData(audioTrack).orElse(null);
        if (trackData == null) {
            return false;
        }
        
        audioPlayer.playTrack(audioTrack);
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        embedBuilder.setTitle("Now playing");
        embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
        MessageManager.sendTemporaryMessage(trackData.getChannel(), embedBuilder.build());
        return true;
    }
    
    public static boolean canPlayNext(Guild guild) {
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(guild);
        return (audioPlayer.getPlayingTrack() == null || audioPlayer.getPlayingTrack().getInfo().isStream) && guild.getAudioManager().isConnected();
    }
    
    public static void exception(MessageChannel channel, FriendlyException exception) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
        embedBuilder.setTitle("Encountered an error");
        embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(exception.getMessage(), "Unknown"));
        embedBuilder.setFooter("Details available in console", null);
        MessageManager.sendTemporaryMessage(channel, embedBuilder.build());
    }
    
    public static void playlist(AudioPlaylist audioPlaylist) throws IllegalArgumentException {
        if (audioPlaylist.isSearchResult()) {
            int searchLimit = DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getSearchLimit).orElse(GeneralCategory.DEFAULT_SEARCH_LIMIT);
            Map<AudioTrackData, List<AudioTrack>> data = Maps.newHashMap();
            for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                AudioTrackData audioTrackData = getData(audioTrack).orElse(null);
                if (audioTrackData == null) {
                    DiscordMusic.getInstance().getLogger().warn("AudioTrack from AudioPlaylist is missing AudioTrackData");
                    continue;
                }
                
                data.compute(audioTrackData, (key, value) -> {
                    if (value != null) {
                        if (value.size() < searchLimit) {
                            value.add(audioTrack);
                        }
                        
                        return value;
                    } else {
                        return Lists.newArrayList(audioTrack);
                    }
                });
            }
            
            for (Map.Entry<AudioTrackData, List<AudioTrack>> entry : data.entrySet()) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                
                for (int index = 0; index < entry.getValue().size(); index++) {
                    AudioTrack audioTrack = entry.getValue().get(index);
                    if (embedBuilder.getDescriptionBuilder().length() != 0) {
                        embedBuilder.getDescriptionBuilder().append("\n");
                    }
                    
                    embedBuilder.getDescriptionBuilder()
                            .append("**").append(index + 1).append(".**")
                            .append(" [").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
                }
                
                ReactionMenu.Builder menuBuilder = new ReactionMenu.Builder();
                menuBuilder.addReactions(Arrays.copyOf(OrderedMenu.NUMBERS, entry.getValue().size()));
                menuBuilder.addUsers(entry.getKey().getUser());
                menuBuilder.setAction(event -> {
                    int index = Arrays.binarySearch(OrderedMenu.NUMBERS, event.getReactionEmote().getName());
                    if (index < 0 || index >= entry.getValue().size()) {
                        return false;
                    }
                    
                    AudioManager.track(entry.getValue().get(index));
                    return true;
                });
                menuBuilder.setFinalAction(message -> {
                    message.delete().queue();
                    getSearchResults(message.getGuild()).remove(entry.getKey());
                });
                menuBuilder.setEventWaiter(AccountManager.EVENT_WAITER);
                DiscordMusic.getInstance().getConfig()
                        .map(Config::getMessageCategory)
                        .map(MessageCategory::getActionTimeout)
                        .ifPresent(timeout -> menuBuilder.setTimeout(timeout, TimeUnit.MILLISECONDS));
                menuBuilder.build().display(entry.getKey().getChannel(), embedBuilder.build());
                
                getSearchResults(entry.getKey().getGuild()).put(entry.getKey(), entry.getValue());
            }
            
            return;
        }
        
        if (audioPlaylist.getSelectedTrack() != null) {
            track(audioPlaylist.getSelectedTrack());
            return;
        }
        
        if (!audioPlaylist.getTracks().isEmpty()) {
            Map<AudioTrackData, List<AudioTrack>> data = Maps.newHashMap();
            for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                AudioTrackData audioTrackData = getData(audioTrack).orElse(null);
                if (audioTrackData == null) {
                    DiscordMusic.getInstance().getLogger().warn("AudioTrack from AudioPlaylist is missing AudioTrackData");
                    continue;
                }
                
                // noinspection Convert2MethodRef
                data.computeIfAbsent(audioTrackData, key -> Lists.newArrayList()).add(audioTrack);
            }
            
            for (Map.Entry<AudioTrackData, List<AudioTrack>> entry : data.entrySet()) {
                for (AudioTrack audioTrack : entry.getValue()) {
                    getAudioQueue(entry.getKey().getGuild()).offer(audioTrack);
                }
                
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                embedBuilder.setTitle(entry.getValue().size() + " tracks added to queue");
                MessageManager.sendTemporaryMessage(entry.getKey().getChannel(), embedBuilder.build());
                
                if (canPlayNext(entry.getKey().getGuild())) {
                    playNext(entry.getKey().getGuild());
                }
            }
            
            return;
        }
        
        DiscordMusic.getInstance().getLogger().warn("Failed to handle AudioPlaylist");
    }
    
    public static void track(AudioTrack audioTrack) throws IllegalArgumentException {
        AudioTrackData trackData = getData(audioTrack).orElseThrow(() -> new IllegalArgumentException("AudioTrack is missing TrackData"));
        getAudioQueue(trackData.getGuild()).offer(audioTrack);
        if (canPlayNext(trackData.getGuild())) {
            playNext(trackData.getGuild());
            return;
        }
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle("Track added to queue");
        embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
        MessageManager.sendTemporaryMessage(trackData.getChannel(), embedBuilder.build());
    }
    
    public static Optional<AudioTrackData> getData(AudioTrack audioTrack) {
        return Optional.ofNullable(audioTrack.getUserData(AudioTrackData.class));
    }
    
    public static AudioPlayer getAudioPlayer(Guild guild) {
        return AUDIO_PLAYERS.get(guild.getIdLong());
    }
    
    public static BlockingQueue<AudioTrack> getAudioQueue(Guild guild) {
        return AUDIO_QUEUES.get(guild.getIdLong());
    }
    
    public static List<AudioTrack> getSearchResult(Member member) {
        Map<AudioTrackData, List<AudioTrack>> searchResults = getSearchResults(member.getGuild());
        if (searchResults == null) {
            return null;
        }
        
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (searchResults) {
            for (Map.Entry<AudioTrackData, List<AudioTrack>> entry : searchResults.entrySet()) {
                if (entry.getKey().getUser().getIdLong() == member.getIdLong()) {
                    return entry.getValue();
                }
            }
            
            return null;
        }
    }
    
    public static void removeSearchResult(Member member) {
        Map<AudioTrackData, List<AudioTrack>> searchResults = getSearchResults(member.getGuild());
        if (searchResults == null) {
            return;
        }
        
        searchResults.keySet().removeIf(audioTrackData -> audioTrackData.getUser().getIdLong() == member.getIdLong());
    }
    
    public static Map<AudioTrackData, List<AudioTrack>> getSearchResults(Guild guild) {
        return SEARCH_RESULTS.get(guild.getIdLong());
    }
}