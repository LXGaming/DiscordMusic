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

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.AccountCategory;
import io.github.lxgaming.discordmusic.listener.DiscordListener;
import io.github.lxgaming.discordmusic.listener.EventWaiterListener;
import io.github.lxgaming.discordmusic.util.StringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public final class AccountManager {
    
    public static final EventWaiter EVENT_WAITER = new EventWaiter(TaskManager.SCHEDULED_EXECUTOR_SERVICE, false);
    private static JDA jda;
    
    public static void prepare() {
        AccountCategory accountCategory = DiscordMusic.getInstance().getConfig().map(Config::getAccountCategory).orElseThrow(NullPointerException::new);
        createJDA(accountCategory.getToken());
        reload();
    }
    
    public static boolean reload() {
        AccountCategory accountCategory = DiscordMusic.getInstance().getConfig().map(Config::getAccountCategory).orElse(null);
        if (accountCategory == null) {
            DiscordMusic.getInstance().getLogger().warn("AccountCategory is unavailable");
            return false;
        }
        
        JDA jda = getJDA();
        if (jda == null) {
            DiscordMusic.getInstance().getLogger().warn("JDA is unavailable");
            return false;
        }
        
        Activity activity;
        if (StringUtils.isNotBlank(accountCategory.getActivityTitle()) && accountCategory.getActivityType() != null) {
            activity = Activity.of(accountCategory.getActivityType(), accountCategory.getActivityTitle());
        } else {
            activity = null;
        }
        
        OnlineStatus onlineStatus;
        if (accountCategory.getOnlineStatus() != OnlineStatus.UNKNOWN) {
            onlineStatus = accountCategory.getOnlineStatus();
        } else {
            onlineStatus = null;
        }
        
        jda.getPresence().setPresence(onlineStatus, activity);
        return true;
    }
    
    public static void shutdown() {
        JDA jda = getJDA();
        if (jda != null) {
            jda.shutdown();
        }
    }
    
    private static void createJDA(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                throw new IllegalArgumentException("Token cannot be blank");
            }
            
            JDABuilder jdaBuilder = JDABuilder.create(token, Sets.newHashSet(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS
            ));
            
            jdaBuilder.addEventListeners(new DiscordListener(), new EventWaiterListener());
            jdaBuilder.disableCache(CacheFlag.EMOTE);
            jdaBuilder.setBulkDeleteSplittingEnabled(false);
            jdaBuilder.setEnableShutdownHook(false);
            jdaBuilder.setEventManager(new AnnotatedEventManager());
            jda = jdaBuilder.build();
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while creating JDA", ex);
        }
    }
    
    public static JDA getJDA() {
        return jda;
    }
}