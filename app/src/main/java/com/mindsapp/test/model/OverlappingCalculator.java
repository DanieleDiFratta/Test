package com.mindsapp.test.model;

import java.util.Collection;
import java.util.List;

/**
 * Created by Daniele on 11/12/2015.
 */
public class OverlappingCalculator {
    //Canali Wireless                      1    2    3    4    5    6    7    8    9   10    11    12    13
    private final int[][] overlapping = {{22,  17,  12,   7,   2,   0,   0,   0,   0,   0,    0,    0,    0},// 1
                                         {17,  22 , 17,  12,   7,   2,   0,   0,   0,   0,    0,    0,    0},// 2
                                         {12,  17,  22,  17,  12,   7,   2,   0,   0,   0,    0,    0,    0},// 3
                                         { 7,  12,  17,  22,  17,  12,   7,   2,   0,   0,    0,    0,    0},// 4
                                         { 2,   7,  12,  17,  22,  17,  12,   7,   2,   0,    0,    0,    0},// 5
                                         { 0,   2,   7,  12,  17,  22,  17,  12,   7,   2,    0,    0,    0},// 6
                                         { 0,   0,   2,   7,  12,  17,  22,  17,  12,   7,    2,    0,    0},// 7
                                         { 0,   0,   0,   2,   7,  12,  17,  22,  17,  12,    7,    2,    0},// 8
                                         { 0,   0,   0,   0,   2,   7,  12,  17,  22,  17,   12,    7,    2},// 9
                                         { 0,   0,   0,   0,   0,   2,   7,  12,  17,  22,   17,   12,    7},//10
                                         { 0,   0,   0,   0,   0,   0,   2,   7,  12,  17,   22,   17,   12},//11
                                         { 0,   0,   0,   0,   0,   0,   0,   2,   7,  12,   17,   22,   17},//12
                                         { 0,   0,   0,   0,   0,   0,   0,   0,   2,   7,   12,   17,   22} //13
    };

    public OverlappingCalculator() {

    }

    public void calculateOverlapping(Collection<Channel> channels) {
        for(Channel actual_ch : channels) {
            int id_channel = actual_ch.getId() - 1;
            int temp_overlapping = 0;
            for (Channel scanned_ch : channels) {
                if (!scanned_ch.getNetworks().isEmpty()) {
                    int temp_id = scanned_ch.getId() - 1;
                    temp_overlapping += overlapping[id_channel][temp_id];
                }
            }
            actual_ch.setOverlapping(temp_overlapping);
        }
    }

//    public Channel minorOverlapping(Collection<Channel> channels){
//        int overlapping = MAX_OVERLAPPING;
//        Channel bestChannel = new Channel();
//        for (Channel ch:
//             channels) {
//            if(ch.getOverlapping()<overlapping){
//                overlapping = ch.getOverlapping();
//                bestChannel = ch;
//            }
//        }
//        return bestChannel;
//    }

}
