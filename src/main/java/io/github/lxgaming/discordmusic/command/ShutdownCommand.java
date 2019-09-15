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
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ShutdownCommand extends AbstractCommand {
    
    public ShutdownCommand() {
        addAlias("shutdown");
        setDescription("Safely shutdowns the bot.");
        setPermission("shutdown.base");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
        embedBuilder.setTitle("Shutting down...");
        embedBuilder.setFooter(Toolbox.getTimeString(Duration.between(DiscordMusic.getInstance().getStartTime(), Instant.now()).toMillis()), null);
        MessageManager.sendMessage(message.getChannel(), embedBuilder.build());
        Runtime.getRuntime().exit(0);
    }
}