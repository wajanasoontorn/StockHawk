package com.udacity.stockhawk;

import java.util.Calendar;

/**
 * Created by wajanasoontorn on 4/27/17.
 */

public class Utils {

    public static int getMonthsDifference(Calendar calendar1, Calendar calendar2) {
        int m1 = calendar1.get(Calendar.YEAR) * 12 + calendar1.get(Calendar.MONTH);
        int m2 = calendar2.get(Calendar.YEAR) * 12 + calendar2.get(Calendar.MONTH);
        return m2 - m1 + 1;
    }

}
