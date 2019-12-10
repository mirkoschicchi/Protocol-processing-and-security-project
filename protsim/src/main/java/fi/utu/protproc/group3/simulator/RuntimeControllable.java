package fi.utu.protproc.group3.simulator;

public interface RuntimeControllable {
    boolean isOnline();
    void start();
    void shutdown();
}
