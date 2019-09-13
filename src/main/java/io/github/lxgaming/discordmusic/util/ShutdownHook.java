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

package io.github.lxgaming.discordmusic.util;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.manager.AccountManager;
import io.github.lxgaming.discordmusic.manager.ServiceManager;
import net.dv8tion.jda.core.JDA;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.TimeUnit;

public class ShutdownHook extends Thread {
    
    @Override
    public void run() {
        Thread.currentThread().setName("Shutdown Thread");
        DiscordMusic.getInstance().getLogger().info("Shutting down...");
        shutdownExecutorService();
        shutdownJDA();
        LogManager.shutdown();
    }
    
    private void shutdownExecutorService() {
        try {
            ServiceManager.getScheduledExecutorService().shutdown();
            if (!ServiceManager.getScheduledExecutorService().awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                throw new InterruptedException();
            }
            
            DiscordMusic.getInstance().getLogger().info("Successfully terminated threads, continuing with shutdown process...");
        } catch (InterruptedException | RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Failed to terminate threads, continuing with shutdown process...");
        }
    }
    
    private void shutdownJDA() {
        AccountManager.getJDA().ifPresent(JDA::shutdown);
        DiscordMusic.getInstance().getLogger().info("JDA Shutdown");
    }
}