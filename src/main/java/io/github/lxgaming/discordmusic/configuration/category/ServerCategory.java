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

package io.github.lxgaming.discordmusic.configuration.category;

import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.Set;

public class ServerCategory {
    
    private long id;
    private String name;
    private long autoJoinChannel;
    private Set<GroupCategory> groups;
    
    public ServerCategory() {
        setId(0L);
        setName("Unknown");
        setAutoJoinChannel(0L);
        setGroups(Toolbox.newLinkedHashSet());
    }
    
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
    
    public void setAutoJoinChannel(long autoJoinChannel) {
        this.autoJoinChannel = autoJoinChannel;
    }
    
    public Set<GroupCategory> getGroups() {
        return groups;
    }
    
    public void setGroups(Set<GroupCategory> groups) {
        this.groups = groups;
    }
}