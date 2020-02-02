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

package io.github.lxgaming.discordmusic.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Collection;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    
    public static int countMatches(String string, String searchString) {
        if (string == null || string.length() == 0 || searchString == null || searchString.length() == 0) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        while ((index = string.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }
        
        return count;
    }
    
    public static boolean containsIgnoreCase(Collection<String> collection, String targetString) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        
        for (String string : collection) {
            if (string.equalsIgnoreCase(targetString)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String truncate(String string, int length, String suffix) {
        if (string.length() >= length) {
            return string.substring(0, length) + suffix;
        }
        
        return string;
    }
    
    public static String toString(Object object) {
        if (object != null) {
            return object.toString();
        }
        
        return "null";
    }
    
    public static String toString(JsonElement jsonElement) throws UnsupportedOperationException {
        if (jsonElement instanceof JsonPrimitive) {
            return jsonElement.getAsString();
        }
        
        if (jsonElement instanceof JsonArray) {
            return toString((JsonArray) jsonElement);
        }
        
        throw new UnsupportedOperationException(String.format("%s is not supported", jsonElement.getClass().getSimpleName()));
    }
    
    public static String toString(JsonArray jsonArray) throws UnsupportedOperationException {
        StringBuilder stringBuilder = new StringBuilder();
        for (JsonElement jsonElement : jsonArray) {
            if (!(jsonElement instanceof JsonPrimitive)) {
                throw new UnsupportedOperationException(String.format("%s is not supported inside a JsonArray", jsonElement.getClass().getSimpleName()));
            }
            
            if (stringBuilder.length() != 0) {
                stringBuilder.append("\n");
            }
            
            stringBuilder.append(jsonElement.getAsString());
        }
        
        return stringBuilder.toString();
    }
}