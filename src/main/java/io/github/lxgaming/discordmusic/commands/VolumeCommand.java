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

import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.Optional;

public class VolumeCommand extends AbstractCommand {
    
    public VolumeCommand() {
        addAlias("volume");
        setDescription("Adjusts playback volume.");
        setUsage("[0 ~ 100]");
        setPermission("command.volume");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Toolbox.DEFAULT);
        if (arguments.isEmpty()) {
            embedBuilder.setTitle("Volume - " + AudioManager.getAudioPlayer(member.getGuild()).getVolume());
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        Optional<Integer> volume = Toolbox.parseInteger(arguments.get(0));
        if (!volume.isPresent()) {
            embedBuilder.setColor(Toolbox.ERROR);
            embedBuilder.setTitle("Failed to parse argument");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        if (volume.get() < 0 || volume.get() > 100) {
            embedBuilder.setColor(Toolbox.WARNING);
            embedBuilder.setTitle("Value is outside of the allowed range (0 ~ 100)");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        AudioManager.getAudioPlayer(member.getGuild()).setVolume(volume.get());
        embedBuilder.setColor(Toolbox.SUCCESS);
        embedBuilder.setTitle("Volume - " + AudioManager.getAudioPlayer(member.getGuild()).getVolume());
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}