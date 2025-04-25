package com.uniq.tms.tms_microservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "@#$%^&*!?";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + NUMBERS + SPECIAL_CHARACTERS;
    private static final int PASSWORD_LENGTH = 8;

    private static final SecureRandom random = new SecureRandom();

    public static String generateDefaultPassword() {

        StringBuilder password = new StringBuilder();
        password.append(getRandomChar(UPPERCASE));
        password.append(getRandomChar(LOWERCASE));
        password.append(getRandomChar(NUMBERS));
        password.append(getRandomChar(SPECIAL_CHARACTERS));

        password.append(IntStream.range(0, PASSWORD_LENGTH - 4)
                .mapToObj(i -> String.valueOf(getRandomChar(ALL_CHARACTERS)))
                .collect(Collectors.joining()));

        return shuffleString(password.toString());
    }

    private static char getRandomChar(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    public static String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean isPasswordMatch(String rawPassword, String storedHashedPassword) {
        return passwordEncoder.matches(rawPassword, storedHashedPassword);
    }
}
