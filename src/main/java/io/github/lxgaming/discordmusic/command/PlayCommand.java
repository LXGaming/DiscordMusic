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
import io.github.lxgaming.discordmusic.entity.AudioTrackData;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.handler.AudioPlayerLoadResultHandler;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.net.URL;
import java.util.List;
import java.util.Set;

public class PlayCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("play");
        description("Plays audio from the specific URLs.");
        usage("[URL...]");
        permission("play.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        if (arguments.isEmpty()) {
            Command command = CommandManager.getCommand(HelpCommand.class);
            if (command != null) {
                command.execute(message, getPath());
            }
            
            return;
        }
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.setTitle("Play");
        for (String string : arguments) {
            URL url = Toolbox.parseUrl(string);
            if (url == null) {
                embedBuilder.getDescriptionBuilder().append("**Invalid:** ").append(string).append("\n");
                continue;
            }
            
            if (!StringUtils.equals(url.getProtocol(), "https")) {
                embedBuilder.getDescriptionBuilder().append("**Unsecure:** ").append(string).append("\n");
                continue;
            }
            
            Set<String> allowedSources = DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getAllowedSources).orElse(null);
            if (allowedSources == null || !allowedSources.contains(url.getHost())) {
                embedBuilder.getDescriptionBuilder().append("**Forbidden:** ").append(string).append("\n");
                continue;
            }
            
            embedBuilder.getDescriptionBuilder().append("**Processing**: ").append(string).append("\n");
            AudioManager.AUDIO_PLAYER_MANAGER.loadItem(string, new AudioPlayerLoadResultHandler(AudioTrackData.of(message)));
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}