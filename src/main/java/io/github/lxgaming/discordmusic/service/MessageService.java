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

package io.github.lxgaming.discordmusic.service;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
import java.util.Iterator;

public class MessageService extends AbstractService {
    
    @Override
    public boolean prepare() {
        setInterval(1000L);
        return true;
    }
    
    @Override
    public void execute() {
        Instant deleteTime = DiscordMusic.getInstance().getConfig()
                .map(Config::getMessageCategory)
                .map(MessageCategory::getDeleteInternal)
                .map(deleteInternal -> Instant.now().minusMillis(deleteInternal))
                .orElse(null);
        if (deleteTime == null) {
            DiscordMusic.getInstance().getLogger().warn("Failed to calculate delete time");
            return;
        }
        
        synchronized (MessageManager.MESSAGES) {
            for (Iterator<Message> iterator = MessageManager.MESSAGES.iterator(); iterator.hasNext(); ) {
                Message message = iterator.next();
                if (message.getIdLong() == 0L) {
                    iterator.remove();
                    continue;
                }
                
                if (message.getTimeCreated().toInstant().isAfter(deleteTime)) {
                    continue;
                }
                
                message.delete().queue(
                        success -> DiscordMusic.getInstance().getLogger().debug("Successfully deleted Message {}", message.getIdLong()),
                        failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error deleting message {}", message.getIdLong(), failure)
                );
                
                iterator.remove();
            }
        }
    }
}