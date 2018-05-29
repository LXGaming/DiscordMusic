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

package io.github.lxgaming.discordmusic.managers;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class MessageManager {
    
    private static final Set<Message> MESSAGES = Collections.synchronizedSet(Toolbox.newLinkedHashSet());
    
    public static void sendMessage(TextChannel textChannel, MessageEmbed messageEmbed, boolean delete) {
        sendMessage(textChannel, new MessageBuilder().setEmbed(messageEmbed).build(), delete);
    }
    
    public static void sendMessage(TextChannel textChannel, Message message, boolean delete) {
        try {
            Objects.requireNonNull(textChannel, "TextChannel cannot be null");
            Objects.requireNonNull(message, "Message cannot be null");
            
            textChannel.sendMessage(message).queue(success -> {
                if (DiscordMusic.getInstance().getConfig().map(Config::isDeleteMessages).orElse(false) && delete) {
                    getMessages().add(success);
                }
            }, failure -> {
                DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::sendMessage", "MessageManager", failure);
            });
        } catch (RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::sendMessage", "MessageManager", ex);
        }
    }
    
    public static void removeMessages(Collection<String> messages) {
        getMessages().removeIf(message -> messages.contains(message.getId()));
    }
    
    public static Set<Message> getMessages() {
        return MESSAGES;
    }
}