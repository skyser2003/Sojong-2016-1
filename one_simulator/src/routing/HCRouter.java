package routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import sojong.HCGroup;

public class HCRouter extends ActiveRouter {
    static private ArrayList<HCGroup> groupList = new ArrayList<>();
    static private ArrayList<HCRouter> routerList = new ArrayList<>();

    static int warmupTime = 0;
    static boolean warmUpEnded = false;

    static boolean onePerGroup = true;
    static int k = 10;

    private int groupID = -1;
    private boolean isCenter = false;

    private HashMap<DTNHost, Integer> meetCount = new HashMap<>();

    static private boolean isWarmUp() {
        return warmupTime > SimClock.getTime();
    }

    static private void warmUpEnd() {
        List<HCRouter> removeList = new ArrayList<>();

        for (HCRouter router : routerList) {
            if (router.getHost() == null) {
                removeList.add(router);
                continue;
            }

            router.clearMessages();
        }

        for (HCRouter router : removeList) {
            routerList.remove(router);
        }

        for (HCRouter r1 : routerList) {
            for (HCRouter r2 : routerList) {
                if (r1.meetCount.containsKey(r2.getHost()) == false) {
                    continue;
                }
                if (r1.groupID != -1 && r2.groupID != -1) {
                    continue;
                }

                int meetShareTargetCount = 0;

                for (Entry<DTNHost, Integer> entry : r1.meetCount.entrySet()) {
                    DTNHost host = entry.getKey();

                    if (host == r2.getHost()) {
                        continue;
                    }

                    if (r2.meetCount.containsKey(host) == true) {
                        ++meetShareTargetCount;
                    }
                }

                if (meetShareTargetCount >= 5) {
                    if (r1.groupID == -1 && r2.groupID == -1) {
                        int groupID = groupList.size();
                        groupList.add(new HCGroup());

                        r1.groupID = groupID;
                        r2.groupID = groupID;

                        groupList.get(groupID).routerList.add(r1);
                        groupList.get(groupID).routerList.add(r2);
                    } else if (r1.groupID != -1) {
                        int groupID = r1.groupID;

                        r2.groupID = groupID;

                        groupList.get(groupID).routerList.add(r2);
                    } else if (r2.groupID != -1) {
                        int groupID = r2.groupID;

                        r1.groupID = groupID;

                        groupList.get(groupID).routerList.add(r1);
                    }
                }
            }
        }

        int centerCount = 0;

        outerLoop:
        while (true) {
            boolean allNull = true;

            for (HCGroup group : groupList) {
                int largestMeetCount = -1;
                HCRouter largestMeetRouter = null;

                for (HCRouter r1 : group.routerList) {
                    if (group.centerList.contains(r1) == true) {
                        continue;
                    }

                    int totalMeetCount = 0;

                    for (Entry<DTNHost, Integer> entry : r1.meetCount.entrySet()) {
                        totalMeetCount += entry.getValue();
                    }

                    if (largestMeetCount == -1 || largestMeetCount < totalMeetCount) {
                        largestMeetCount = totalMeetCount;
                        largestMeetRouter = r1;
                    }
                }

                if (largestMeetRouter != null) {
                    allNull = false;
                } else {
                    continue;
                }

                group.centerList.add(largestMeetRouter);
                largestMeetRouter.isCenter = true;
                largestMeetRouter.createMessage("the_single_message");

                ++centerCount;
                if (centerCount == k) {
                    break outerLoop;
                }
            }

            if (onePerGroup == true || allNull == true) {
                break;
            }
        }
    }

    public HCRouter(Settings s) {
        super(s);

        initSelf();
    }

    protected HCRouter(ActiveRouter r) {
        super(r);

        initSelf();
    }

    private void initSelf() {
        routerList.add(this);
        warmupTime = new Settings(report.Report.REPORT_NS).getInt(report.Report.WARMUP_S);
    }

    private void meetCountUp(DTNHost other) {
        int count = 0;

        if (meetCount.containsKey(other) == true) {
            count = meetCount.get(other) + 1;
        } else {
            count = 1;
        }

        meetCount.put(other, count);
    }

    private void createMessage(String id) {
        Message m = new Message(getHost(), null, id, 0);
        m.setResponseSize(0);
        createNewMessage(m);
    }

    @Override
    protected int checkReceiving(Message m, DTNHost from) {
        int recvCheck = super.checkReceiving(m, from);

        if (recvCheck == RCV_OK) {
            if (isWarmUp() == true) {
                HCRouter otherRouter = (HCRouter) from.getRouter();

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
        return new HCRouter(this);
    }
}