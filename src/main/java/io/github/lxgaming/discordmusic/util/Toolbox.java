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

import io.github.lxgaming.common.concurrent.BasicThreadFactory;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Toolbox {
    
    /**
     * Removes non-printable characters (excluding new line and carriage return) in the provided {@link java.lang.String String}.
     *
     * @param string The {@link java.lang.String String} to filter.
     * @return The filtered {@link java.lang.String String}.
     */
    public static String filter(String string) {
        return string.replaceAll("[^\\x20-\\x7E\\x0A\\x0D]", "");
    }
    
    public static String escapeMarkdown(String sequence) {
        return MarkdownSanitizer.sanitize(sequence)
                .replace("[", "\\[").replace("]", "\\]")
                .replace("(", "\\(").replace(")", "\\)");
    }
    
    public static void removeMentions(Message message, List<String> arguments) {
        for (MessageChannel channel : message.getMentionedChannels()) {
            arguments.removeIf(argument -> argument.equals("#" + channel.getName()));
        }
        
        for (Role role : message.getMentionedRoles()) {
            arguments.removeIf(argument -> argument.equals("@" + role.getName()));
        }
        
        for (User user : message.getMentionedUsers()) {
            arguments.removeIf(argument -> argument.equals("@" + user.getName()));
        }
    }
    
    public static String getDuration(long value, TimeUnit unit, boolean abbreviate, TimeUnit precision) {
        return getDuration(precision.convert(value, unit), precision, abbreviate);
    }
    
    public static String getDuration(long value, TimeUnit unit, boolean abbreviate) {
        StringBuilder stringBuilder = new StringBuilder();
        if (TimeUnit.DAYS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toDays(value), TimeUnit.DAYS, abbreviate);
        }
        
        if (TimeUnit.HOURS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toHours(value) % 24, TimeUnit.HOURS, abbreviate);
        }
        
        if (TimeUnit.MINUTES.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toMinutes(value) % 60, TimeUnit.MINUTES, abbreviate);
        }
        
        if (TimeUnit.SECONDS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toSeconds(value) % 60, TimeUnit.SECONDS, abbreviate);
        }
        
        if (TimeUnit.MILLISECONDS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toMillis(value) % 1000, TimeUnit.MILLISECONDS, abbreviate);
        }
        
        if (TimeUnit.MICROSECONDS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toMicros(value) % 1000, TimeUnit.MICROSECONDS, abbreviate);
        }
        
        if (TimeUnit.NANOSECONDS.compareTo(unit) >= 0) {
            append(stringBuilder, unit.toNanos(value) % 1000, TimeUnit.NANOSECONDS, abbreviate);
        }
        
        return stringBuilder.toString();
    }
    
    public static void append(StringBuilder stringBuilder, long value, TimeUnit unit, boolean abbreviate) {
        if (value <= 0) {
            return;
        }
        
        if (stringBuilder.length() > 0) {
            stringBuilder.append(abbreviate ? " " : ", ");
        }
        
        stringBuilder.append(value);
        if (!abbreviate) {
            stringBuilder.append(" ");
        }
        
        switch (unit) {
            case NANOSECONDS:
                stringBuilder.append(abbreviate ? "ns" : value == 1 ? "nanosecond" : "nanoseconds");
                break;
            case MICROSECONDS:
                stringBuilder.append(abbreviate ? "\u03BCs" : value == 1 ? "microsecond" : "microseconds");
                break;
            case MILLISECONDS:
                stringBuilder.append(abbreviate ? "ms" : value == 1 ? "millisecond" : "milliseconds");
                break;
            case SECONDS:
                stringBuilder.append(abbreviate ? "s" : value == 1 ? "second" : "seconds");
                break;
            case MINUTES:
                stringBuilder.append(abbreviate ? "min" : value == 1 ? "minute" : "minutes");
                break;
            case HOURS:
                stringBuilder.append(abbreviate ? "h" : value == 1 ? "hour" : "hours");
                break;
            case DAYS:
                stringBuilder.append(abbreviate ? "d" : value == 1 ? "day" : "days");
                break;
        }
    }
    
    public static Integer parseInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    public static Long parseLong(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    public static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    
    public static String getClassSimpleName(Class<?> type) {
        if (type.getEnclosingClass() != null) {
            return getClassSimpleName(type.getEnclosingClass()) + "." + type.getSimpleName();
        }
        
        return type.getSimpleName();
    }
    
    public static <T> T newInstance(Class<? extends T> type) {
        try {
            return type.newInstance();
        } catch (Throwable ex) {
            return null;
        }
    }
    
    public static Path getPath() {
        String userDir = System.getProperty("user.dir");
        if (StringUtils.isNotBlank(userDir)) {
            return Paths.get(userDir);
        }
        
        return Paths.get(".");
    }
    
    public static ThreadFactory newThreadFactory(String format) {
        return BasicThreadFactory.builder().daemon(true).format(format).priority(Thread.NORM_PRIORITY).build();
    }
}