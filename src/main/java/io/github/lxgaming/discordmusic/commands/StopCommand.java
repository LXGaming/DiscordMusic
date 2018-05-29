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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class StopCommand extends AbstractCommand {
    
    public StopCommand() {
        addAlias("stop");
        setDescription("Stops the Player");
        setPermission("command.stop");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Toolbox.DEFAULT);
        
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(member.getGuild());
        if (audioPlayer == null) {
            embedBuilder.setColor(Toolbox.ERROR);
            embedBuilder.setTitle("Failed to get AudioPlayer");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        if (audioPlayer.getPlayingTrack() == null) {
            embedBuilder.setColor(Toolbox.WARNING);
            embedBuilder.setTitle("Player is not playing anything");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        audioPlayer.stopTrack();
        embedBuilder.setColor(Toolbox.SUCCESS);
        embedBuilder.setTitle("Player stopped.");
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}