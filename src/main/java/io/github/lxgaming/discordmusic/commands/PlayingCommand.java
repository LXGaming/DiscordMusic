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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

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
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(DiscordUtil.DEFAULT);
        
        AudioTrack audioTrack = AudioManager.getAudioPlayer(member.getGuild()).getPlayingTrack();
        if (audioTrack == null) {
            embedBuilder.setColor(DiscordUtil.WARNING);
            embedBuilder.setTitle("Nothing is currently playing.");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        embedBuilder.setColor(DiscordUtil.SUCCESS);
        embedBuilder.setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri);
        if (audioTrack.getInfo().isStream) {
            embedBuilder.setFooter(DiscordUtil.getTimeString(audioTrack.getPosition()), null);
        } else {
            embedBuilder.setFooter(DiscordUtil.getTimeString(audioTrack.getPosition()) + " / " + DiscordUtil.getTimeString(audioTrack.getDuration()), null);
        }
        
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}