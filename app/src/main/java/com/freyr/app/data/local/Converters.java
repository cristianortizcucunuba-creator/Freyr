package com.freyr.app.data.local;

import androidx.room.TypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        String[] items = value.split(",");
        List<String> list = new ArrayList<>();
        for (String item : items) {
            if (!item.trim().isEmpty()) {
                list.add(item.trim());
            }
        }
        return list;
    }
}
