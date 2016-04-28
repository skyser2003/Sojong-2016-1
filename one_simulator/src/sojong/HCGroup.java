package sojong;

import java.util.ArrayList;

import routing.HCRouter;

public class HCGroup {
    public ArrayList<HCRouter> centerList = new ArrayList<>();
    public ArrayList<HCRouter> routerList = new ArrayList<>();

    private ArrayList<HCGroup> childGroupList = new ArrayList<>();

    static public HCGroup merge(HCGroup group1, HCGroup group2) {
        HCGroup ret = new HCGroup();

        ret.routerList.addAll(group1.routerList);
        ret.routerList.addAll(group2.routerList);

        return ret;
    }

    public int maxWeight(HCGroup other) {
        int maxWeight = 0;

        ArrayList<HCRouter> allRouterList = allRouterList();
        ArrayList<HCRouter> otherRouterList = other.allRouterList();

        for(HCRouter r1 : allRouterList) {
            int localMax = 0;

            for(HCRouter r2 : otherRouterList) {
                if(r1.meetCount(r2) != 0){
                    ++localMax;
                }
            }

            maxWeight = Math.max(maxWeight, localMax);
        }

        return maxWeight;
    }

    public void addChild(HCGroup child) {
        if(childGroupList.contains(child) == false) {
            childGroupList.add(child);
        }
    }

    public ArrayList<HCRouter> allRouterList() {
        ArrayList<HCRouter> ret = new ArrayList<>();

        for(HCGroup child : childGroupList) {
            ret.addAll(child.allRouterList());
        }

        if(ret.size() == 0) {
            ret.addAll(routerList);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "Router count : " + routerList.size() + ", childCount : " + childGroupList.size();
    }
}