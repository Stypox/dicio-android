package com.dicio.dicio_android.util;

import java.util.Iterator;
import java.util.List;

public class StringUtils {

    public static String join(String delimiter, List<String> strings) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = strings.iterator();

        if (iterator.hasNext()) {
            builder.append(iterator.next());
        }
        while (iterator.hasNext()) {
            builder.append(delimiter);
            builder.append(iterator.next());
        }

        return builder.toString();
    }
}
