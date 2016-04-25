package sojong;

import java.util.ArrayList;

import routing.HCRouter;

public class HCGroup {
    public ArrayList<HCRouter> centerList = new ArrayList<>();
    public ArrayList<HCRouter> routerList = new ArrayList<>();

    public int meetCount = -1;

    @Override
    public String toString() {
        return "Router count : " + routerList.size() + ", meetCount : " + meetCount;
    }
}