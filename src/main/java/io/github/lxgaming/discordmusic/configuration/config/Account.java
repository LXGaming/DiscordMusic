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

package io.github.lxgaming.discordmusic.configuration.config;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

public class Account {
    
    private String token;
    private String gameTitle;
    private Game.GameType gameType;
    private transient JDA jda;
    
    public Account() {
        setToken("");
        setGameTitle("Music");
        setGameType(Game.GameType.LISTENING);
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }
    
    public Game.GameType getGameType() {
        return gameType;
    }
    
    public void setGameType(Game.GameType gameType) {
        this.gameType = gameType;
    }
    
    public JDA getJDA() {
        return jda;
    }
    
    public void setJDA(JDA jda) {
        this.jda = jda;
    }
}