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

package io.github.lxgaming.discordmusic.command;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Optional;

public class SelectCommand extends AbstractCommand {
    
    public SelectCommand() {
        addAlias("select");
        setDescription("Select from search results.");
        setPermission("select.base");
        setUsage("<ID...>");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        if (arguments.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Invalid arguments");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        List<AudioTrack> searchResult = AudioManager.getSearchResult(message.getMember());
        if (searchResult == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("You don't have any search results pending selection.");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        List<AudioTrack> audioTracks = Toolbox.newArrayList();
        for (String string : arguments) {
            if (embedBuilder.getDescriptionBuilder().length() != 0) {
                embedBuilder.getDescriptionBuilder().append("\n");
            }
            
            Optional<Integer> selection = Toolbox.parseInteger(string);
            if (!selection.isPresent()) {
                embedBuilder.getDescriptionBuilder().append("**Invalid**: ").append(string);
                continue;
            }
            
            if (selection.get() < 1 || selection.get() > searchResult.size()) {
                embedBuilder.getDescriptionBuilder().append("**OutOfBounds**: ").append(selection.get());
                continue;
            }
            
            AudioTrack audioTrack = searchResult.get(selection.get() - 1);
            audioTracks.add(audioTrack);
            embedBuilder.getDescriptionBuilder().append("**Processing**: ").append(audioTrack.getInfo().title);
        }
        
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle("Select results");
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
        if (audioTracks.isEmpty()) {
            return;
        }
        
        AudioManager.removeSearchResult(message.getMember());
        if (audioTracks.size() > 1) {
            AudioPlaylist audioPlaylist = new BasicAudioPlaylist("Search", audioTracks, null, false);
            AudioManager.playlist(audioPlaylist);
        } else {
            AudioManager.track(audioTracks.get(0));
        }
    }
}