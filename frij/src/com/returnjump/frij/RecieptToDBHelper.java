package com.returnjump.frij;

import java.util.List;

/**
 * Created by Kelsey on 6/29/2014.
 */
public class RecieptToDBHelper {

    public static final int MAX_EDIT_DISTANCE_THRESHOLD = 20;
    public static final int WIGGLE_ROOM =2;

    public static int editDistance (String recieptItem, String dbItem) {
        dbItem=dbItem.toLowerCase();
        recieptItem=recieptItem.toLowerCase();
        int dbItemLength = dbItem.length()+1;
        int recieptItemLength = recieptItem.length()+1;

        int insertCost = 10;
        int deleteCost = 1;

        if(recieptItem.length() < dbItem.length()-WIGGLE_ROOM)
            return Integer.MAX_VALUE;
        
        // the array of distances
        int[] cost = new int[dbItemLength];
        int[] newcost = new int[dbItemLength];

        // initial cost of skipping prefix
        for(int i=0; i<dbItemLength; i++) {
            cost[i] = i * insertCost;
        }

        for(int rIndex=1; rIndex<recieptItemLength; rIndex++) {

            newcost[0]=rIndex-1;

            for(int dIndex=1;dIndex<dbItemLength;dIndex++) {

                int match = (dbItem.charAt(dIndex-1)==recieptItem.charAt(rIndex-1)) ? 0 : 10;
                int cost_replace = cost[dIndex-1] + match;
                int cost_insert  = newcost[dIndex-1] + insertCost;
                int cost_delete  = cost[dIndex] + deleteCost;

                newcost[dIndex] = Math.min(Math.min(cost_insert, cost_delete),cost_replace);
            }
            // swap cost/newcost arrays
            int[] swap=cost; cost=newcost; newcost=swap;
        }
        // the distance is the cost for transforming all letters in both strings
        return cost[dbItemLength-1];
    }


    // If there's a tie, this will return the first item with the lowest cost
    public static String minimumEditDistance(List<String> items, String target) {
        String minimum = target;
        int minCost = Integer.MAX_VALUE;

        for (String item: items) {
            int cost = editDistance(item, target);

            if (cost < minCost) {
                minimum = item;
                minCost = cost;
            }
        }

        return minCost < MAX_EDIT_DISTANCE_THRESHOLD ? minimum : null;
    }

}
