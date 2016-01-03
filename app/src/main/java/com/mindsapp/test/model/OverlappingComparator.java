package com.mindsapp.test.model;

import java.util.Comparator;

/**
 * Created by Daniele on 14/12/2015.
 */
public class OverlappingComparator implements Comparator<Channel> {
    @Override
    public int compare(Channel lhs, Channel rhs) {
        return lhs.getOverlapping()-rhs.getOverlapping();
    }
}
