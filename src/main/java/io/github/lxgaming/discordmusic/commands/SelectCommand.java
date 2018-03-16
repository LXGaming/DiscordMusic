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

package io.github.lxgaming.discordmusic.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordData;
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SelectCommand extends AbstractCommand {
    
    public SelectCommand() {
        addAlias("select");
        setDescription("Select from search results.");
        setPermission("command.select");
        setUsage("<ID...>");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(DiscordUtil.DEFAULT);
        
        if (arguments.isEmpty()) {
            embedBuilder.setColor(DiscordUtil.ERROR);
            embedBuilder.setTitle("Invalid arguments");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        Map.Entry<DiscordData, List<AudioTrack>> searchResult = AudioManager.getSearchResult(member);
        if (searchResult == null) {
            embedBuilder.setColor(DiscordUtil.WARNING);
            embedBuilder.setTitle("You don't have any search results pending selection.");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        List<AudioTrack> audioTracks = DiscordUtil.newArrayList();
        for (String string : arguments) {
            if (embedBuilder.getDescriptionBuilder().length() != 0) {
                embedBuilder.getDescriptionBuilder().append("\n");
            }
            
            Optional<Integer> selection = DiscordUtil.parseInteger(string);
            if (!selection.isPresent()) {
                embedBuilder.getDescriptionBuilder().append("**Invalid**: ").append(string);
                continue;
            }
            
            if (selection.get() < 1 || selection.get() > searchResult.getValue().size()) {
                embedBuilder.getDescriptionBuilder().append("**OutOfBounds**: ").append(selection.get());
                continue;
            }
            
            AudioTrack audioTrack = searchResult.getValue().get(selection.get() - 1);
            audioTracks.add(audioTrack);
            embedBuilder.getDescriptionBuilder().append("**Processing**: ").append(audioTrack.getInfo().title);
        }
        
        embedBuilder.setTitle("Select results");
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
        if (audioTracks.isEmpty()) {
            return;
        }
        
        AudioManager.removeSearchResult(member);
        if (audioTracks.size() > 1) {
            AudioPlaylist audioPlaylist = new BasicAudioPlaylist("Search", audioTracks, null, false);
            AudioManager.playlist(new DiscordData(message, textChannel, member), audioPlaylist);
        } else {
            AudioManager.track(new DiscordData(message, textChannel, member), audioTracks.get(0));
        }
    }
}