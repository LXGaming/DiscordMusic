/*
 * Copyright 2021 Alex Thomson
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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A Custom {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation.
 */
public abstract class Menu extends com.jagrosh.jdautilities.menu.Menu {
    
    protected Menu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit) {
        super(waiter, users, roles, timeout, unit);
    }
    
    public abstract void display(MessageChannel channel, String content);
    
    public abstract void display(MessageChannel channel, MessageEmbed messageEmbed);
    
    protected abstract void initialize(RestAction<Message> restAction);
    
    /**
     * Not supported.
     *
     * @deprecated Use {@link #display(MessageChannel, String)} or {@link #display(MessageChannel, MessageEmbed)} instead.
     */
    @Deprecated
    @Override
    public final void display(MessageChannel channel) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Not supported.
     *
     * @deprecated Use {@link #display(MessageChannel, String)} or {@link #display(MessageChannel, MessageEmbed)} instead.
     */
    @Deprecated
    @Override
    public final void display(Message message) {
        throw new UnsupportedOperationException();
    }
}