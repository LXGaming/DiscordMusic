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

package io.github.lxgaming.discordmusic.configuration;

import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.discordmusic.configuration.category.AccountCategory;
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.configuration.category.GuildCategory;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.configuration.category.ServiceCategory;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.Set;

public class Config {
    
    @SerializedName("account")
    private AccountCategory accountCategory = new AccountCategory();
    
    @SerializedName("general")
    private GeneralCategory generalCategory = new GeneralCategory();
    
    @SerializedName("guilds")
    private Set<GuildCategory> guildCategories = Toolbox.newHashSet();
    
    @SerializedName("message")
    private MessageCategory messageCategory = new MessageCategory();
    
    @SerializedName("service")
    private ServiceCategory serviceCategory = new ServiceCategory();
    
    public AccountCategory getAccountCategory() {
        return accountCategory;
    }
    
    public GeneralCategory getGeneralCategory() {
        return generalCategory;
    }
    
    public Set<GuildCategory> getGuildCategories() {
        return guildCategories;
    }
    
    public MessageCategory getMessageCategory() {
        return messageCategory;
    }
    
    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }
}