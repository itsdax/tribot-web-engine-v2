package scripts.dax.walker.engine.interaction;

import java.util.regex.Pattern;

public class Constants {
    // Equipment that has teleports
    public static final Pattern RING_OF_WEALTH_MATCHER = Pattern.compile("(?i)ring of wealth.?\\(.+");
    public static final Pattern RING_OF_DUELING_MATCHER = Pattern.compile("(?i)ring of dueling.?\\(.+");
    public static final Pattern NECKLACE_OF_PASSAGE_MATCHER = Pattern.compile("(?i)necklace of passage.?\\(.+");
    public static final Pattern COMBAT_BRACE_MATCHER = Pattern.compile("(?i)combat brace.+\\(.+");
    public static final Pattern GAMES_NECKLACE_MATCHER = Pattern.compile("(?i)game.+neck.+\\(.+");
    public static final Pattern GLORY_MATCHER = Pattern.compile("(?i).+glory.*\\(.+");
}
