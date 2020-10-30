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

package io.github.lxgaming.discordmusic.configuration.category;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class GuildCategory {
    
    @SerializedName("id")
    private long id = 0L;
    
    @SerializedName("name")
    private String name = "Unknown";
    
    @SerializedName("autoJoinChannel")
    private long autoJoinChannel = 0L;
    
    @SerializedName("autoPause")
    private boolean autoPause = false;
    
    @SerializedName("autoPlay")
    private boolean autoPlay = false;
    
    @SerializedName("roles")
    private Set<RoleCategory> roleCategories = Sets.newCopyOnWriteArraySet();
    
    @SerializedName("users")
    private Set<UserCategory> userCategories = Sets.newCopyOnWriteArraySet();
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getAutoJoinChannel() {
        return autoJoinChannel;
    }
    
    public boolean isAutoPause() {
        return autoPause;
    }
    
    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    public Set<RoleCategory> getRoleCategories() {
        return roleCategories;
    }
    
    public Set<UserCategory> getUserCategories() {
        return userCategories;
    }
}