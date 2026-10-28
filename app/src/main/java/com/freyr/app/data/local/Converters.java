package com.freyr.app.data.local;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));

            if (i < list.size() - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] items = value.split(",");
        List<String> result = new ArrayList<>();

        for (String item : items) {
            if (!item.trim().isEmpty()) {
                result.add(item.trim());
            }
        }

        return result;
    }
}