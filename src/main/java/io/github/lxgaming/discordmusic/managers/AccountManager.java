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
import io.github.lxgaming.discordmusic.configuration.config.Account;
import io.github.lxgaming.discordmusic.listeners.DiscordListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;
import java.util.Optional;

public class AccountManager {
    
    public static void buildAccount() {
        DiscordMusic.getInstance().getConfig().map(Config::getAccount).ifPresent(AccountManager::createJDA);
    }
    
    private static void createJDA(Account account) {
        try {
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            jdaBuilder.addEventListener(new DiscordListener());
            jdaBuilder.setBulkDeleteSplittingEnabled(false);
            jdaBuilder.setEnableShutdownHook(false);
            jdaBuilder.setToken(account.getToken());
            account.setJDA(jdaBuilder.buildAsync());
        } catch (LoginException | RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::createJDA", "AccountManager", ex);
        }
    }
    
    public static Optional<JDA> getJDA() {
        return DiscordMusic.getInstance().getConfig().map(Config::getAccount).map(Account::getJDA);
    }
}