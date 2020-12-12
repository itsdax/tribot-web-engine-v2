package scripts.dax.common;

import lombok.AllArgsConstructor;

import java.awt.*;

import static java.lang.String.format;
import static org.tribot.api.General.println;
import static scripts.dax.common.DaxLogger.Level.*;

public final class DaxLogger {

    private static Level globalLogLevel = DEBUG;

    public static void setLogLevel(Level logLevel) {
        globalLogLevel = logLevel;
    }

    public static void debug(String message, Object... args) {
        outputLog(DEBUG, format(message, args));
    }

    public static void info(String message, Object... args) {
        outputLog(INFO, format(message, args));
    }

    public static void warn(String message, Object... args) {
        outputLog(WARN, format(message, args));
    }

    public static void error(String message, Object... args) {
        outputLog(ERROR, format(message, args));
    }

    private static void outputLog(Level level, String message) {
        if (!level.gte(globalLogLevel)) return;
        println(format("[%s] %s", level.shortHand(), message), level.fg, level.bg);
    }

    @AllArgsConstructor
    public enum Level {
        DEBUG(0, new Color(0, 0, 0), new Color(191, 255, 197)),
        INFO(1, new Color(0, 0, 0), new Color(206, 206, 206)),
        WARN(2, new Color(0, 0, 0), new Color(255, 217, 148)),
        ERROR(3, new Color(0, 0, 0), new Color(255, 188, 188));
        final int severity;
        final Color fg, bg;

        boolean gte(Level level) {
            return this.severity >= level.severity;
        }

        String shortHand() {
            return this.toString().substring(0, 1);
        }
    }
}
