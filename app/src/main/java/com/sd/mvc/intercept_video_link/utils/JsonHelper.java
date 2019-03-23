package com.sd.mvc.intercept_video_link.utils;

import com.google.gson.Gson;

public class JsonHelper {
    private static Gson gson = new Gson();

    public static String jsonToString(Object obj) {
        return gson.toJson(obj);
    }

    public static Object stringToJson(String json, Class clazz) {
        return gson.fromJson(json, clazz);
    }
}
