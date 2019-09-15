/*
 * Copyright 2019 Alex Thomson
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

package io.github.lxgaming.discordmusic.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Custom {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation.
 */
public class CustomMenu extends Menu {
    
    private final Set<String> choices;
    private final Function<MessageReactionAddEvent, Boolean> action;
    private final Consumer<Message> finalAction;
    
    private CustomMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                       Set<String> choices, Function<MessageReactionAddEvent, Boolean> action,
                       Consumer<Message> finalAction) {
        super(waiter, users, roles, timeout, unit);
        this.choices = choices;
        this.action = action;
        this.finalAction = finalAction;
    }
    
    /**
     * Not supported.
     *
     * @deprecated Use {@link #display(MessageChannel, Message)} instead.
     */
    @Deprecated
    @Override
    public void display(MessageChannel channel) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Not supported.
     *
     * @deprecated Use {@link #display(MessageChannel, Message)} instead.
     */
    @Deprecated
    @Override
    public void display(Message message) {
        throw new UnsupportedOperationException();
    }
    
    public void display(MessageChannel channel, CharSequence charSequence) {
        initialize(channel.sendMessage(charSequence));
    }
    
    public void display(MessageChannel channel, MessageEmbed messageEmbed) {
        initialize(channel.sendMessage(messageEmbed));
    }
    
    public void display(MessageChannel channel, Message message) {
        initialize(channel.sendMessage(message));
    }
    
    private void initialize(RestAction<Message> restAction) {
        restAction.queue(success -> {
            for (String choice : choices) {
                Emote emote = getEmote(success.getJDA(), choice);
                if (emote != null) {
                    success.addReaction(emote).queue();
                } else {
                    success.addReaction(choice).queue();
                }
            }
            
            waitForEvent(success);
        });
    }
    
    private void waitForEvent(Message message) {
        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
            if (event.getMessageIdLong() != message.getIdLong()) {
                return false;
            }
            
            String reaction;
            if (event.getReactionEmote().isEmote()) {
                reaction = event.getReactionEmote().getId();
            } else {
                reaction = event.getReactionEmote().getName();
            }
            
            if (!choices.contains(reaction)) {
                return false;
            }
            
            return isValidUser(event.getUser(), event.getGuild());
        }, (MessageReactionAddEvent event) -> {
            if (action.apply(event)) {
                waitForEvent(message);
            } else {
                finalAction.accept(message);
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }
    
    private Emote getEmote(JDA jda, String id) {
        try {
            return jda.getEmoteById(id);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static class Builder extends Menu.Builder<Builder, CustomMenu> {
        
        private final Set<String> choices = Toolbox.newLinkedHashSet();
        private Function<MessageReactionAddEvent, Boolean> action;
        private Consumer<Message> finalAction = message -> {
        };
        
        @Override
        public CustomMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(finalAction != null, "Must provide an final action consumer");
            return new CustomMenu(waiter, users, roles, timeout, unit, choices, action, finalAction);
        }
        
        public Builder addChoices(String... emojis) {
            Collections.addAll(this.choices, emojis);
            return this;
        }
        
        public Builder addChoices(Emote... emotes) {
            for (Emote emote : emotes) {
                addChoices(emote.getId());
            }
            
            return this;
        }
        
        public Builder setAction(Function<MessageReactionAddEvent, Boolean> action) {
            this.action = action;
            return this;
        }
        
        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }
    }
}