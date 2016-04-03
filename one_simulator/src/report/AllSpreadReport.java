package report;

import java.util.HashSet;
import java.util.List;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class AllSpreadReport extends Report implements UpdateListener {
	private boolean written = false;

	public AllSpreadReport() {
		init();
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
				break;
			} else {
				++count;
			}
		}

		if (allSpread == true) {
			written = true;
			write("All spread! time : " + SimClock.getTime());
		} else {
			write("time : " + SimClock.getTime() + ", Current spread : " + count + ", total count : " + hosts.size());
		}
	}

}