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
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Color;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;
import java.util.Optional;

public class DebugCommand extends AbstractCommand {
    
    public DebugCommand() {
        addAlias("debug");
        setDescription("For development purposes.");
        setPermission("command.debug");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message.getJDA().getSelfUser().getName(), null, message.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        
        Optional<Config> config = DiscordMusic.getInstance().getConfig();
        if (!config.isPresent()) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Configuration error");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        if (arguments.isEmpty()) {
            if (config.get().isDebug()) {
                config.get().setDebug(false);
                DiscordMusic.getInstance().reloadLogger();
                embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
                embedBuilder.setTitle("Debugging disabled");
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            config.get().setDebug(true);
            DiscordMusic.getInstance().reloadLogger();
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Debugging enabled");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
        }
    }
}