package it.unisa.telerehab.app;

/**
 * Plain launcher used by the IDE "Run" button.
 *
 * <p>Starting a class that extends {@code Application} directly from the classpath
 * fails with "JavaFX runtime components are missing". A main class that does not
 * extend {@code Application} avoids the problem. From the terminal use
 * {@code mvn javafx:run} instead.</p>
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
