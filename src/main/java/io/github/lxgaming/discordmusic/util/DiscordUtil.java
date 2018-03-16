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

import net.dv8tion.jda.core.entities.ISnowflake;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiscordUtil {
    
    public static final Color DEFAULT = decodeColor("#7289DA").orElse(null);
    public static final Color SUCCESS = decodeColor("#46A84B").orElse(null);
    public static final Color WARNING = decodeColor("#EAA245").orElse(null);
    public static final Color ERROR = decodeColor("#C13737").orElse(null);
    
    /**
     * Decodes the provided {@link java.lang.String String} into a {@link java.awt.Color Color}.
     *
     * @param string The {@link java.lang.String String} to decode.
     * @return The {@link java.awt.Color Color}.
     */
    public static Optional<Color> decodeColor(String string) {
        try {
            return Optional.of(Color.decode(string));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    
    /**
     * Removes non-printable characters (excluding new line and carriage return) in the provided {@link java.lang.String String}.
     *
     * @param string The {@link java.lang.String String} to filter.
     * @return The filtered {@link java.lang.String String}.
     */
    public static String filter(String string) {
        return StringUtils.replaceAll(string, "[^\\x20-\\x7E\\x0A\\x0D]", "");
    }
    
    public static String getTimeString(long time) {
        time = Math.abs(time);
        long second = time / 1000;
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
            
            stringBuilder.append(unit).append(" ");
            if (unit == 1) {
                stringBuilder.append(singular);
            } else {
                stringBuilder.append(plural);
            }
        }
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
    
    public static boolean containsIgnoreCase(Collection<String> list, String targetString) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        
        for (String string : list) {
            if (StringUtils.equalsIgnoreCase(string, targetString)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static <T> Optional<T> newInstance(Class<? extends T> typeOfT) {
        try {
            return Optional.of(typeOfT.newInstance());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
    
    public static ThreadFactory buildThreadFactory(String namingPattern) {
        return new BasicThreadFactory.Builder().namingPattern(namingPattern).daemon(true).priority(Thread.NORM_PRIORITY).build();
    }
    
    public static Optional<Long> getIdLong(ISnowflake snowflake) {
        if (snowflake != null) {
            return Optional.of(snowflake.getIdLong());
        }
        
        return Optional.empty();
    }
    
    public static Optional<Path> getPath() {
        String userDir = System.getProperty("user.dir");
        if (StringUtils.isNotBlank(userDir)) {
            return Optional.of(Paths.get(userDir));
        }
        
        return Optional.empty();
    }
    
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) throws NullPointerException {
        Objects.requireNonNull(elements);
        return Stream.of(elements).collect(Collectors.toCollection(ArrayList::new));
    }
    
    @SafeVarargs
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(E... elements) throws NullPointerException {
        Objects.requireNonNull(elements);
        return Stream.of(elements).collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }
    
    @SafeVarargs
    public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) throws NullPointerException {
        Objects.requireNonNull(elements);
        return Stream.of(elements).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }
}