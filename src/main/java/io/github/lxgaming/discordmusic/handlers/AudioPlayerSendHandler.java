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

package io.github.lxgaming.discordmusic.handlers;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;

public final class AudioPlayerSendHandler implements AudioSendHandler {
    
    private final Guild guild;
    private AudioFrame audioFrame;
    
    public AudioPlayerSendHandler(Guild guild) {
        this.guild = guild;
    }
    
    @Override
    public boolean canProvide() {
        if (getAudioFrame() == null) {
            setAudioFrame(AudioManager.getAudioPlayer(getGuild()).provide());
        }
        
        return getAudioFrame() != null;
    }
    
    @Override
    public byte[] provide20MsAudio() {
        try {
            if (getAudioFrame() != null) {
                return getAudioFrame().getData();
            }
            
            return null;
        } finally {
            setAudioFrame(null);
        }
    }
    
    @Override
    public boolean isOpus() {
        return true;
    }
    
    private Guild getGuild() {
        return guild;
    }
    
    private AudioFrame getAudioFrame() {
        return audioFrame;
    }
    
    private void setAudioFrame(AudioFrame audioFrame) {
        this.audioFrame = audioFrame;
    }
}