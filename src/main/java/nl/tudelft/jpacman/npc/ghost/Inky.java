package nl.tudelft.jpacman.npc.ghost;

import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.Sprite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An implementation of the classic Pac-Man ghost Bashful.
 * </p>
 * <p>
 * Nickname: Inky. Bashful has truly earned his name. In a game of chicken
 * between Pac-Man and Bashful, Bashful might just run away. This isn't always
 * the case, but if Blinky is right behind you, it's a safe bet. He can't be
 * scared off while he patrols the southeast portion of the maze. In Japan, his
 * name is Kimagure/Aosuke.
 * </p>
 * <p>
 * <b>AI:</b> Bashful has the most complicated AI of all. When the ghosts are
 * not patrolling their home corners, Bashful considers two things: Shadow's
 * location, and the location two grid spaces ahead of Pac-Man. Bashful draws a
 * line from Shadow to the spot that is two squares in front of Pac-Man and
 * extends that line twice as far. Therefore, if Bashful is alongside Shadow
 * when they are behind Pac-Man, Bashful will usually follow Shadow the whole
 * time. But if Bashful is in front of Pac-Man when Shadow is far behind him,
 * Bashful tends to want to move away from Pac-Man (in reality, to a point very
 * far ahead of Pac-Man). Bashful is affected by a similar targeting bug that
 * affects Speedy. When Pac-Man is moving or facing up, the spot Bashful uses to
 * draw the line is two squares above and left of Pac-Man.
 * </p>
 * <p>
 * Source: http://strategywiki.org/wiki/Pac-Man/Getting_Started
 * </p>
 * 
 * @author Jeroen Roosen <j.roosen@student.tudelft.nl>
 * 
 */
public class Inky extends Ghost {

	private static final int SQUARES_AHEAD = 2;

	/**
	 * The variation in intervals, this makes the ghosts look more dynamic and
	 * less predictable.
	 */
	private static final int INTERVAL_VARIATION = 50;

	/**
	 * The base movement interval.
	 */
	private static final int MOVE_INTERVAL = 250;

	/**
	 * The log.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(Inky.class);

	/**
	 * Creates a new "Inky", a.k.a. Bashful.
	 * 
	 * @param spriteMap
	 *            The sprites for this ghost.
	 */
	public Inky(Map<Direction, Sprite> spriteMap) {
		super(spriteMap);
	}

	@Override
	public long getInterval() {
		return MOVE_INTERVAL + new Random().nextInt(INTERVAL_VARIATION);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Bashful has the most complicated AI of all. When the ghosts are not
	 * patrolling their home corners, Bashful considers two things: Shadow's
	 * location, and the location two grid spaces ahead of Pac-Man. Bashful
	 * draws a line from Shadow to the spot that is two squares in front of
	 * Pac-Man and extends that line twice as far. Therefore, if Bashful is
	 * alongside Shadow when they are behind Pac-Man, Bashful will usually
	 * follow Shadow the whole time. But if Bashful is in front of Pac-Man when
	 * Shadow is far behind him, Bashful tends to want to move away from Pac-Man
	 * (in reality, to a point very far ahead of Pac-Man). Bashful is affected
	 * by a similar targeting bug that affects Speedy. When Pac-Man is moving or
	 * facing up, the spot Bashful uses to draw the line is two squares above
	 * and left of Pac-Man.
	 * </p>
	 * 
	 * <p>
	 * <b>Implementation:</b> by lack of a coordinate system there is a
	 * workaround: first determine the square of Blinky (A) and the square 2
	 * squares away from Pac-Man (B). Then determine the shortest path from A to
	 * B regardless of terrain and walk that same path from B. This is the
	 * destination.
	 * </p>
	 */
	@Override
	public Direction nextMove() {
		long t0 = System.currentTimeMillis();
		Unit blinky = Navigation.findNearest(Blinky.class, getSquare());
		if (blinky == null) {
			LOG.debug("Could not find Blinky, will move around randomly.");
			Direction d = randomMove();
			LOG.debug("Moving {} (calculated in {}ms)", d,
					System.currentTimeMillis() - t0);
			return d;
		}
		LOG.debug("Found Blinky (position A)");

		Unit player = Navigation.findNearest(Player.class, getSquare());
		if (player == null) {
			LOG.debug("Could not find Player, will move around randomly.");
			Direction d = randomMove();
			LOG.debug("Moving {} (calculated in {}ms)", d,
					System.currentTimeMillis() - t0);
			return d;
		}
		LOG.debug("Player found.");

		Direction targetDirection = player.getDirection();
		Square playerDestination = player.getSquare();
		for (int i = 0; i < SQUARES_AHEAD; i++) {
			playerDestination = playerDestination.getSquareAt(targetDirection);
		}
		LOG.debug("Calculated position B: {} squares {} of Player.",
				SQUARES_AHEAD, targetDirection);

		Square destination = playerDestination;
		List<Direction> firstHalf = Navigation.shortestPath(blinky.getSquare(),
				playerDestination, null);
		if (firstHalf == null) {
			LOG.debug("Could not find a path from (Pos A) to (Pos B), will move randomly.");
			Direction d = randomMove();
			LOG.debug("Moving {} (calculated in {}ms)", d,
					System.currentTimeMillis() - t0);
			return d;
		}

		for (Direction d : firstHalf) {
			destination = playerDestination.getSquareAt(d);
		}
		LOG.debug("Calculated position C, moving there.");

		List<Direction> path = Navigation.shortestPath(getSquare(),
				destination, this);
		if (path != null && !path.isEmpty()) {
			Direction d = path.get(0);
			LOG.debug(
					"Found path to destination. Moving {} (calculated in {}ms)",
					d, System.currentTimeMillis() - t0);
			return d;
		}
		LOG.debug("Could not find path to destination, will move around randomly.");
		Direction d = randomMove();
		LOG.debug("Moving {} (calculated in {}ms)", d,
				System.currentTimeMillis() - t0);
		return d;
	}

}