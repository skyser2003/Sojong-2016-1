package routing;

import core.Message;
import core.Settings;
import core.SimClock;

import java.util.ArrayList;

public class RandomRouter extends ActiveRouter {
    static ArrayList<RandomRouter> routerList = new ArrayList<>();

    static private double warmUpTime = 0.0;
    static private boolean initialized = false;
    static private int centerNodeCount = 10;

    static private void warmUpEnd() {
        ArrayList<RandomRouter> removeList = new ArrayList<>();

        for (RandomRouter r : routerList) {
            if (r.getHost() == null) {
                removeList.add(r);
                continue;
            }

            r.clearMessages();
        }

        for (RandomRouter r : removeList) {
            routerList.remove(r);
        }

        ArrayList<RandomRouter> centerNodeList = new ArrayList<>();

        while (true) {
            int index = (int) (Math.random() * routerList.size());
            RandomRouter r = routerList.get(index);

            if (centerNodeList.contains(r) == true) {
                continue;
            }

            centerNodeList.add(r);

            if (centerNodeList.size() == centerNodeCount) {
                break;
            }
        }

        for(RandomRouter r : centerNodeList) {
            r.createMessage("the-single-message");
        }
    }

    public RandomRouter(ActiveRouter router) {
        super(router);

        initSelf();
    }

    static private boolean isWarmUp() {
        return warmUpTime > SimClock.getTime();
    }

    public RandomRouter(Settings s) {
        super(s);

        initSelf();
    }

    private void initSelf() {
        routerList.add(this);
        warmUpTime = new Settings(report.Report.REPORT_NS).getInt(report.Report.WARMUP_S);
    }

    private void createMessage(String id) {
        Message m = new Message(getHost(), null, id, 0);
        m.setResponseSize(0);
        createNewMessage(m);
    }

    @Override
    public void update() {
        super.update();

        if (initialized == false && isWarmUp() == false) {
            initialized = true;

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
        return new RandomRouter(this);
    }
}