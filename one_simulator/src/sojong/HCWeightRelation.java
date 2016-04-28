package sojong;

import routing.HCRouter;

public class HCWeightRelation implements Comparable<HCWeightRelation> {
    public int weight;
    public HCGroup r1;
    public HCGroup r2;

    public HCWeightRelation(int weight, HCGroup r1, HCGroup r2) {
        this.weight = weight;
        this.r1 = r1;
        this.r2 = r2;
    }

    @Override
    public int compareTo(HCWeightRelation o) {
        if (weight < o.weight) {
            return -1;
        } else if (weight == o.weight) {
            return 0;
        } else {
            return 1;
        }
    }
}