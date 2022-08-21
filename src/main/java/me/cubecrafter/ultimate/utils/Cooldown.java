package me.cubecrafter.ultimate.utils;

public class Cooldown {

    private final long startTime;
    private final long cooldown;

    public Cooldown(int seconds) {
        this.cooldown = seconds * 1000L;
        this.startTime = System.currentTimeMillis();
    }

    private long getTimeElapsedMillis() {
        return System.currentTimeMillis() - startTime;
    }

    private long getMillisLeft() {
        return cooldown - getTimeElapsedMillis();
    }

    public int getSecondsLeft() {
        return (int) Math.ceil((cooldown - getTimeElapsedMillis()) / 1000.0);
    }

    public float getPercentageLeft() {
        return getMillisLeft() / (float) cooldown;
    }

    public boolean isExpired() {
        return getTimeElapsedMillis() >= cooldown;
    }

}
