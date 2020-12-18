package scripts.dax.common;

import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;

public class Distance {

    public static double from(Positionable destination) {
        RSTile position = Player.getPosition();
        if (position == null) return Double.MAX_VALUE;
        return position.distanceTo(destination);
    }


    public static double between(Positionable a, Positionable b) {
        int height = a.getPosition().getY() - b.getPosition().getY();
        int width = a.getPosition().getX() - b.getPosition().getX();
        return Math.abs(Math.sqrt(height * height + width * width));
    }

}
