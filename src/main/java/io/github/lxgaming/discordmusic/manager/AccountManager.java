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

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.AccountCategory;
import io.github.lxgaming.discordmusic.listener.DiscordListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.Optional;

public class AccountManager {
    
    private static JDA jda;
    
    public static void buildAccount() {
        getAccount().ifPresent(AccountManager::createJDA);
    }
    
    public static void reloadAccount() {
        AccountCategory account = getAccount().orElse(null);
        JDA jda = getJDA().orElse(null);
        if (account == null || jda == null) {
            return;
        }
        
        if (account.getGameType() != null && StringUtils.isNotBlank(account.getGameTitle())) {
            jda.getPresence().setActivity(Activity.of(account.getGameType(), account.getGameTitle()));
        }
        
        if (account.getOnlineStatus() != null) {
            jda.getPresence().setStatus(account.getOnlineStatus());
        }
    }
    
    private static void createJDA(AccountCategory account) {
        try {
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            jdaBuilder.addEventListeners(new DiscordListener());
            jdaBuilder.setBulkDeleteSplittingEnabled(false);
            jdaBuilder.setEnableShutdownHook(false);
            jdaBuilder.setToken(account.getToken());
            jda = jdaBuilder.build();
        } catch (LoginException | RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::createJDA", "AccountManager", ex);
        }
    }
    
    public static Optional<AccountCategory> getAccount() {
        return DiscordMusic.getInstance().getConfig().map(Config::getAccount);
    }
    
    public static Optional<JDA> getJDA() {
        return Optional.ofNullable(jda);
    }
}