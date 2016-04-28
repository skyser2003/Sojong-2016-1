package movement;

import core.Coord;
import core.Settings;
import movement.map.MapNode;

import java.util.List;

/**
 * Created by frakafra on 2016. 4. 28..
 */
public class RandomPathMapBasedMovement extends ShortestPathMapBasedMovement {
    private Path generatedPath = null;
    public RandomPathMapBasedMovement(Settings settings) {
        super(settings);
    }

    protected RandomPathMapBasedMovement(ShortestPathMapBasedMovement mbm) {
        super(mbm);
    }

    @Override
    public Path getPath() {
        if (generatedPath != null) {
            return generatedPath;
        }

        Path shortestPath = new Path(generateSpeed());
        generatedPath = new Path(shortestPath.getSpeed());
        MapNode to = pois.selectDestination();

        List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

        // this assertion should never fire if the map is checked in read phase
        assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
                to + ". The simulation map isn't fully connected";

        boolean first = true;
        MapNode prev = null;
        for (MapNode node : nodePath) { // create a Path from the shortest path
            if (first) {
                shortestPath.addWaypoint(node.getLocation());
                first = false;
                prev = node;
                continue;
            }

            if (rng.nextInt(10) == 0) {
                MapNode intermediateNode = pois.selectDestination();
                List<MapNode> ns = pathFinder.getShortestPath(prev, intermediateNode);
                if (ns.size() > 0) {
                    for (MapNode n : ns) {
                        shortestPath.addWaypoint(n.getLocation());
                    }
                    ns = pathFinder.getShortestPath(intermediateNode, node);
                    for (MapNode n : ns) {
                        shortestPath.addWaypoint(n.getLocation());
                    }
                } else {
                    shortestPath.addWaypoint(node.getLocation());
                }
            } else {
                shortestPath.addWaypoint(node.getLocation());
            }

            prev = node;
        }

        lastMapNode = to;
        return generatedPath;
    }

    public RandomPathMapBasedMovement replicate() {
        return new RandomPathMapBasedMovement(this);
    }
}
