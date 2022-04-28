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

package io.github.lxgaming.discordmusic.manager;

import com.google.common.collect.Lists;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.entity.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.internal.entities.DataMessage;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class MessageManager {
    
    public static final String DEFAULT_COLOR = "#7289DA"; // Blurple
    public static final String DEFAULT_EMOTE = "\\u2753"; // ?
    public static final List<Message> MESSAGES = Lists.newCopyOnWriteArrayList();
    
    public static void prepare() {
        MessageCategory messageCategory = DiscordMusic.getInstance().getConfig().map(Config::getMessageCategory).orElseThrow(NullPointerException::new);
        messageCategory.getColors().putIfAbsent(Color.ERROR, "#C13737");
        messageCategory.getColors().putIfAbsent(Color.SUCCESS, "#46A84B");
        messageCategory.getColors().putIfAbsent(Color.WARNING, "#EAA245");
        
        for (Color color : Color.values()) {
            messageCategory.getColors().putIfAbsent(color, DEFAULT_COLOR);
        }
        
        // https://getemoji.com/
        // https://r12a.github.io/app-conversion/
        messageCategory.getEmotes().putIfAbsent(Emote.ACCEPT, "\\u2705");
        messageCategory.getEmotes().putIfAbsent(Emote.DECLINE, "\\u274C");
        
        for (Emote emote : Emote.values()) {
            messageCategory.getEmotes().putIfAbsent(emote, DEFAULT_EMOTE);
        }
    }
    
    public static EmbedBuilder createErrorEmbed(String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(getColor(Color.ERROR));
        embedBuilder.setTitle("An error has occurred.");
        embedBuilder.getDescriptionBuilder().append("```").append(message).append("```");
        return embedBuilder;
    }
    
    public static void sendTemporaryMessage(MessageChannel channel, MessageEmbed messageEmbed) {
        sendTemporaryMessage(channel, new MessageBuilder().setEmbeds(messageEmbed).build());
    }
    
    public static void sendTemporaryMessage(MessageChannel channel, Message message) {
        sendMessage(channel, message, success -> {
            if (DiscordMusic.getInstance().getConfig().map(Config::getMessageCategory).map(MessageCategory::isDeleteMessages).orElse(false)) {
                MESSAGES.add(success);
            }
        });
    }
    
    public static void sendMessage(MessageChannel channel, MessageEmbed messageEmbed) {
        sendMessage(channel, new MessageBuilder().setEmbeds(messageEmbed).build());
    }
    
    public static void sendMessage(MessageChannel channel, Message message) {
        sendMessage(channel, message, success -> {
        });
    }
    
    public static void sendMessage(MessageChannel messageChannel, Message message, Consumer<Message> success) {
        sendMessage(messageChannel, message, success, failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error while sending message", failure));
    }
    
    public static void sendMessage(MessageChannel messageChannel, Message message, Consumer<Message> success, Consumer<Throwable> failure) {
        try {
            messageChannel.sendMessage(message).queue(success, failure);
        } catch (IllegalArgumentException | InsufficientPermissionException | UnsupportedOperationException ex) {
            failure.accept(ex);
        }
    }
    
    public static int getColor(Color color) {
        return DiscordMusic.getInstance().getConfig()
                .map(Config::getMessageCategory)
                .map(MessageCategory::getColors)
                .map(colors -> colors.get(color))
                .map(NumberUtils::createInteger)
                .orElse(Role.DEFAULT_COLOR_RAW);
    }
    
    public static String getEmote(Emote emote) {
        return DiscordMusic.getInstance().getConfig()
                .map(Config::getMessageCategory)
                .map(MessageCategory::getEmotes)
                .map(emotes -> emotes.get(emote))
                .map(StringEscapeUtils::unescapeJava)
                .orElse(DEFAULT_EMOTE);
    }
    
    public static String getMessageContent(Message message) {
        if (message instanceof DataMessage) {
            return message.getContentRaw();
        }
        
        return message.getContentDisplay();
    }
    
    public static void removeMessages(Collection<String> messages) {
        MESSAGES.removeIf(message -> messages.contains(message.getId()));
    }
}