package report;

import java.util.HashSet;
import java.util.List;

import org.json.simple.*;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class AllSpreadReport extends Report implements UpdateListener {
    private boolean written = false;
    private int prev_count = -1;

    public AllSpreadReport() {
        super.init();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (isWarmup() == true || written == true) {
            return;
        }

        boolean allSpread = true;

        int count = 0;

        for (DTNHost host : hosts) {
            if (host.getMessageCollection().size() == 0) {
                allSpread = false;
            } else {
                ++count;
            }
        }

        if (count == prev_count) {
            return;
        } else {
            prev_count = count;
        }

        if (allSpread == true) {
            written = true;
        }

        writeJSON(SimClock.getTime(), count, hosts.size());
    }

    private void writeJSON(double time, int current, int max) {
        JSONObject obj = new JSONObject();
        obj.put("time", time);
        obj.put("current_spread", current);
        obj.put("max_count", max);

        write(obj.toJSONString());
    }
}