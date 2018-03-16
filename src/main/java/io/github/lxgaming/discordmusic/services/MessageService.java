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

package io.github.lxgaming.discordmusic.services;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import net.dv8tion.jda.core.entities.Message;

import java.time.Instant;
import java.util.Iterator;

public class MessageService extends AbstractService {
    
    private long deleteInterval;
    
    public MessageService() {
        setPeriod(5000L);
        setDeleteInterval(60000L);
    }
    
    @Override
    public void execute() {
        synchronized (MessageManager.getMessages()) {
            for (Iterator<Message> iterator = MessageManager.getMessages().iterator(); iterator.hasNext(); ) {
                Message message = iterator.next();
                if (message == null || message.getCreationTime() == null || message.getIdLong() == 0L) {
                    iterator.remove();
                    continue;
                }
                
                if (message.getCreationTime().toInstant().toEpochMilli() > Instant.now().minusMillis(getDeleteInterval()).toEpochMilli()) {
                    continue;
                }
                
                message.delete().queue(
                        success -> DiscordMusic.getInstance().getLogger().debug("Successfully deleted Message {}", message.getIdLong()),
                        failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error deleting Message {}", message.getIdLong(), failure)
                );
                
                iterator.remove();
            }
        }
    }
    
    @Override
    public boolean isPeriodical() {
        return true;
    }
    
    private long getDeleteInterval() {
        return deleteInterval;
    }
    
    private void setDeleteInterval(long deleteInterval) {
        this.deleteInterval = deleteInterval;
    }
}