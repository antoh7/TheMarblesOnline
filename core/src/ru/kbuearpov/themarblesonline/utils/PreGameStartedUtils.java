package ru.kbuearpov.themarblesonline.utils;

import java.util.UUID;

public class PreGameStartedUtils {

    public static String generateRoomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

}
