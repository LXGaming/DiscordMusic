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
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

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
    
    public static String getDuration(long millisecond) {
        long second = Math.abs(millisecond) / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        long day = hour / 24;
        
        StringBuilder stringBuilder = new StringBuilder();
        appendUnit(stringBuilder, day, "day", "days");
        appendUnit(stringBuilder, hour % 24, "hour", "hours");
        appendUnit(stringBuilder, minute % 60, "minute", "minutes");
        appendUnit(stringBuilder, second % 60, "second", "seconds");
        
        if (stringBuilder.length() == 0) {
            stringBuilder.append("just now");
        }
        
        return stringBuilder.toString();
    }
    
    public static void appendUnit(StringBuilder stringBuilder, long unit, String singular, String plural) {
        if (unit > 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            
            stringBuilder.append(unit).append(" ").append(formatUnit(unit, singular, plural));
        }
    }
    
    public static String formatUnit(long unit, String singular, String plural) {
        if (unit == 1) {
            return singular;
        }
        
        return plural;
    }
    
    public static Optional<Integer> parseInteger(String string) {
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static Optional<Long> parseLong(String string) {
        try {
            return Optional.of(Long.parseLong(string));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static Optional<URL> parseUrl(String url) {
        try {
            return Optional.of(new URL(url));
        } catch (MalformedURLException ex) {
            return Optional.empty();
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