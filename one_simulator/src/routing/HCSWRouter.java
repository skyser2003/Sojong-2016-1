package routing;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import routing.util.Pair;

import java.util.*;
import java.util.function.Function;

public class HCSWRouter extends ActiveRouter {
    public static final String HCSW_NS = "HCSWRouter";
    public static int centerNodeCount = 0;
    public static boolean peakNodeByAscending = false;
    public static Function<Pair<HCSWRouter, HashSet<HCSWRouter>>, Float> peakNode = null;
    static private class HCSWGroup {
        public HashSet<HCSWRouter> routers = new HashSet<>();
        public HCSWGroup parentGroup = null;
        public HashSet<HCSWGroup> childrenGroups = new HashSet<>();
        public int level = 0; // single node group : 0

        public HCSWGroup(int groupLevel) {
            level = groupLevel;
        }

        public HCSWGroup(HCSWRouter r) {
            routers.add(r);
        }

        public HCSWGroup merged(HCSWGroup other, int groupLevel) {
            HCSWGroup ret = null;
            if (level == groupLevel) {
                ret = this;
                other.setParent(this);

            } else if (other.level == groupLevel) {
                ret = other;
                setParent(other);
            } else {
                ret = new HCSWGroup(groupLevel);
                setParent(ret);
                other.setParent(ret);
            }
            ret.routers.addAll(routers);
            ret.routers.addAll(other.routers);

            return ret;
        }

        public void setParent(HCSWGroup parent) {
            parentGroup = parent;
            parent.childrenGroups.add(this);
        }

        @Override
        public String toString() {
            return String.format("%d %s", level, super.toString());
        }
    }
    static private ArrayList<HCSWGroup> groupList = new ArrayList<>();
    static private ArrayList<HCSWRouter> routerList = new ArrayList<>();
    static int warmupTime = 0;
    static boolean warmUpEnded = false;
    private boolean isCenter = false;
    private HashMap<DTNHost, Integer> meetCountMap = new HashMap<>();
    static private boolean isWarmUp() {
        return warmupTime > SimClock.getTime();
    }

    static private void warmUpEnd() {
        List<HCSWRouter> removeList = new ArrayList<>();

        for (HCSWRouter router : routerList) {
            if (router.getHost() == null) {
                removeList.add(router);
                continue;
            }

            router.clearMessages();
        }

        for (HCSWRouter router : removeList) {
            routerList.remove(router);
        }

        HashMap<HCSWRouter, HCSWGroup> routerGroupMap = new HashMap<>();
        HashMap<Integer, HashSet<Pair<HCSWRouter, HCSWRouter>>> weightRouterMap = new HashMap<>();
        HashMap<Integer, HashSet<HCSWGroup>> groupsByWeightLevel = new HashMap<>();

        // generate single node groups
        // generate weight - node, node map
        for (HCSWRouter r1 : routerList) {
            routerGroupMap.put(r1, new HCSWGroup(r1));
            for (HCSWRouter r2 : routerList) {
                if (r1 == r2) {
                    continue;
                }
                Integer weight = r1.meetCount(r2);
                if (weight == 0) {
                    continue;
                }
                HashSet<Pair<HCSWRouter, HCSWRouter>> list = weightRouterMap.get(weight);
                if (list == null) {
                    list = new HashSet<>();
                    weightRouterMap.put(weight, list);
                }

                list.add(new Pair<>(r1, r2));
            }
        }

        Integer[] weights = new Integer[weightRouterMap.size()];
        weightRouterMap.keySet().toArray(weights);
        Arrays.sort(weights, Collections.reverseOrder());

        // Hierachical clustering
        for (Integer w : weights) {
            HashSet<HCSWGroup> groups = new HashSet<>();
            groupsByWeightLevel.put(w, groups);
            for (Pair<HCSWRouter, HCSWRouter> routerPair : weightRouterMap.get(w)) {
                HCSWGroup group1 = routerGroupMap.get(routerPair.first),
                        group2 = routerGroupMap.get(routerPair.second);
                if (group1 == group2) {
                    continue;
                }

                HCSWGroup newGroup = group1.merged(group2, w);
                for (HCSWRouter router: newGroup.routers) {
                    routerGroupMap.replace(router, newGroup);
                }
                groups.add(newGroup);
            }
        }

        // Select Center node
        // select largest mean weight node in each cluster
        // find center nodes from high level(largest cluster) to low level
        Integer[] levels = new Integer[groupsByWeightLevel.size() + 1];
        groupsByWeightLevel.keySet().toArray(levels);
        levels[levels.length - 1] = Integer.MAX_VALUE;
        Arrays.sort(levels);
        levels[levels.length - 1] = 0;
        HashSet<HCSWRouter> centerNodes = new HashSet<>();
        loopForLevels:
        for (Integer l : levels) {
            for (HCSWGroup cluster : groupsByWeightLevel.get(l)) {
                HashSet<HCSWRouter> nodes = cluster.routers;
                SortedMap<Float, HCSWRouter> values = new TreeMap<>();
                for (HCSWRouter node : nodes) {
                    if (centerNodes.contains(node)) {
                        continue;
                    }
                    values.put(peakNode.apply(new Pair<>(node, nodes)), node);
                }

                HCSWRouter selectedNode = null;
                if (peakNodeByAscending) {
                    selectedNode = values.get(values.lastKey());
                } else {
                    selectedNode = values.get(values.firstKey());
                }

                if (selectedNode != null) {
                    centerNodes.add(selectedNode);
                    if (centerNodes.size() >= centerNodeCount) {
                        break loopForLevels;
                    }
                }
            }
        }

        for (HCSWRouter centerNode : centerNodes) {
            centerNode.isCenter = true;
            centerNode.createMessage("the_single_message");
        }
    }

    public HCSWRouter(Settings s) {
        super(s);

        initSelf();

        Settings routerSetting = new Settings(HCSW_NS);
        centerNodeCount = routerSetting.getInt("centerNodeCount", centerNodeCount);
        switch (routerSetting.getSetting("centerNodeMethod")) {
            case "mean":
            peakNode = arg -> {
                float mean = 0.0f;
                for (HCSWRouter other : arg.second) {
                    mean += arg.first.meetCount(other);
                }
                mean /= arg.second.size();
                return mean;
            };
        }
        peakNodeByAscending = routerSetting.getBoolean("centerNodeMethodAscending");
    }

    protected HCSWRouter(ActiveRouter r) {
        super(r);

        initSelf();
    }

    private void initSelf() {
        routerList.add(this);
        warmupTime = new Settings(report.Report.REPORT_NS).getInt(report.Report.WARMUP_S);
    }

    private void meetCountUp(DTNHost other) {
        int count = 0;

        if (meetCountMap.containsKey(other) == true) {
            count = meetCountMap.get(other) + 1;
        } else {
            count = 1;
        }

        meetCountMap.put(other, count);
    }

    private void createMessage(String id) {
        Message m = new Message(getHost(), null, id, 0);
        m.setResponseSize(0);
        createNewMessage(m);
    }

    public int meetCount(HCSWRouter other) {
        return meetCountMap.getOrDefault(other.getHost(), 0);
    }

    @Override
    protected int checkReceiving(Message m, DTNHost from) {
        int recvCheck = super.checkReceiving(m, from);

        if (recvCheck == RCV_OK) {
            if (isWarmUp() == true) {
                HCSWRouter otherRouter = (HCSWRouter) from.getRouter();

                meetCountUp(from);
                otherRouter.meetCountUp(getHost());
            }

			/* don't accept a message that has already traversed this node */
            if (m.getHops().contains(getHost())) {
                recvCheck = DENIED_OLD;
            }
        }

        if (isWarmUp() == true) {
            return DENIED_UNSPECIFIED;
        } else {
            return recvCheck;
        }
    }

    @Override
    public void update() {
        super.update();

        if (warmUpEnded == false && isWarmUp() == false) {
            warmUpEnded = true;
            warmUpEnd();
        }

        if (isTransferring() || !canStartTransfer()) {
            return;
        }

        if (exchangeDeliverableMessages() != null) {
            return;
        }

        tryAllMessagesToAllConnections();
    }

    @Override
    public MessageRouter replicate() {
        // TODO Auto-generated method stub
        return new HCSWRouter(this);
    }
}
