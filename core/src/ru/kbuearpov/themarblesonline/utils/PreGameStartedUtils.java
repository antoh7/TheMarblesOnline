package ru.kbuearpov.themarblesonline.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class PreGameStartedUtils {

    public static String generateRoomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

}
