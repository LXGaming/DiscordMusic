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

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class VolumeCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("volume");
        description("Adjusts playback volume.");
        usage("[0 ~ 1000]");
        permission("volume.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        if (arguments.isEmpty()) {
            embedBuilder.setTitle("Volume - " + AudioManager.getAudioPlayer(message.getGuild()).getVolume());
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        Integer volume = Toolbox.parseInteger(arguments.get(0)).orElse(null);
        if (volume == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Failed to parse argument");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        int maxVolume = Math.min(1000, DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getMaxVolume).orElse(GeneralCategory.DEFAULT_MAX_VOLUME));
        if (volume < 0 || volume > maxVolume) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Value is outside of the allowed range (0 ~ " + maxVolume + ")");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        int previousVolume = AudioManager.getAudioPlayer(message.getGuild()).getVolume();
        AudioManager.getAudioPlayer(message.getGuild()).setVolume(volume);
        
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle("Volume - " + previousVolume + " -> " + AudioManager.getAudioPlayer(message.getGuild()).getVolume());
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}