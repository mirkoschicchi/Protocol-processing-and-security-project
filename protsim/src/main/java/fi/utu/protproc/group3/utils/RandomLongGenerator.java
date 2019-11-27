package fi.utu.protproc.group3.utils;

public class RandomLongGenerator {
    public static long generate() {
        long leftLimit = 1L;
        long rightLimit = 4294967295L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }
}
