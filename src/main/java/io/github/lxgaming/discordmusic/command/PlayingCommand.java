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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PlayingCommand extends AbstractCommand {
    
    public PlayingCommand() {
        addAlias("playing");
        addAlias("nowplaying");
        addAlias("np");
        setDescription("Displays the media that is currently being played.");
        setPermission("command.playing");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        
        AudioTrack audioTrack = AudioManager.getAudioPlayer(message.getGuild()).getPlayingTrack();
        if (audioTrack == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Nothing is currently playing.");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri);
        if (audioTrack.getInfo().isStream) {
            embedBuilder.setFooter(Toolbox.getTimeString(audioTrack.getPosition()), null);
        } else {
            embedBuilder.setFooter(Toolbox.getTimeString(audioTrack.getPosition()) + " / " + Toolbox.getTimeString(audioTrack.getDuration()), null);
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}