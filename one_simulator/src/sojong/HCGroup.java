package sojong;

import java.util.ArrayList;

import routing.HCRouter;

public class HCGroup {
    public ArrayList<HCRouter> centerList = new ArrayList<>();
    public ArrayList<HCRouter> routerList = new ArrayList<>();

    public int meetCount = -1;

    static public HCGroup merge(HCGroup group1, HCGroup group2) {
        HCGroup ret = new HCGroup();

        ret.routerList.addAll(group1.routerList);
        ret.routerList.addAll(group2.routerList);
        ret.meetCount = group1.meetCount;

        return ret;
    }

    public int weight(HCGroup other) {
        int count = 0;

        for(HCRouter r1 : routerList) {
            for(HCRouter r2 : other.routerList) {
                count += r1.meetCount(r2);
            }
        }

        return count;
    }

    @Override
    public String toString() {
        return "Router count : " + routerList.size() + ", meetCount : " + meetCount;
    }
}