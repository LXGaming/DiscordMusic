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
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.Optional;

public class DebugCommand extends AbstractCommand {
    
    public DebugCommand() {
        addAlias("debug");
        setDescription("For development purposes.");
        setPermission("command.debug");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(textChannel.getJDA().getSelfUser().getName(), null, textChannel.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(DiscordUtil.DEFAULT);
        
        Optional<Config> config = DiscordMusic.getInstance().getConfig();
        if (!config.isPresent()) {
            embedBuilder.setColor(DiscordUtil.ERROR);
            embedBuilder.setTitle("Configuration error");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        if (arguments.isEmpty()) {
            if (config.get().isDebug()) {
                config.get().setDebug(false);
                DiscordMusic.getInstance().reloadLogger();
                embedBuilder.setColor(DiscordUtil.WARNING);
                embedBuilder.setTitle("Debugging disabled");
                MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
                return;
            }
            
            config.get().setDebug(true);
            DiscordMusic.getInstance().reloadLogger();
            embedBuilder.setColor(DiscordUtil.SUCCESS);
            embedBuilder.setTitle("Debugging enabled");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
        }
    }
}