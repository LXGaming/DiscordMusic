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

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.handlers.AudioPlayerLoadResultHandler;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordData;
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PlayCommand extends AbstractCommand {
    
    public PlayCommand() {
        addAlias("play");
        setDescription("Plays audio from the specific URLs.");
        setUsage("[URL...]");
        setPermission("command.play");
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
        
        embedBuilder.setColor(DiscordUtil.SUCCESS);
        embedBuilder.setTitle("Play");
        for (String string : arguments) {
            Optional<URL> url = DiscordUtil.parseUrl(string);
            if (!url.isPresent()) {
                embedBuilder.getDescriptionBuilder().append("**Invalid:** ").append(string).append("\n");
                continue;
            }
            
            if (!StringUtils.equals(url.get().getProtocol(), "https")) {
                embedBuilder.getDescriptionBuilder().append("**Unsecure:** ").append(string).append("\n");
                continue;
            }
            
            Optional<Set<String>> allowedSources = DiscordMusic.getInstance().getConfig().map(Config::getAllowedSources);
            if (!allowedSources.isPresent() || !allowedSources.get().contains(url.get().getHost())) {
                embedBuilder.getDescriptionBuilder().append("**Forbidden:** ").append(string).append("\n");
                continue;
            }
            
            embedBuilder.getDescriptionBuilder().append("**Processing**: ").append(string).append("\n");
            AudioManager.getAudioPlayerManager().loadItem(string, new AudioPlayerLoadResultHandler(new DiscordData(message, textChannel, member)));
        }
        
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}