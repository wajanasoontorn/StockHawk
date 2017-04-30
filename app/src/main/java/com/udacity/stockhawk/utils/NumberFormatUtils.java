package com.udacity.stockhawk.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by wajanasoontorn on 4/30/17.
 */

public class NumberFormatUtils {
    private static NumberFormatUtils mInstance = null;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    private NumberFormatUtils() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    public static NumberFormatUtils getInstance() {
        if (mInstance == null) {
            mInstance = new NumberFormatUtils();
        }
        return mInstance;
    }

    public String getDollarWithPlusString(float value) {
        return this.dollarFormatWithPlus.format(value);
    }

    public String getDollarString(float value) {
        return this.dollarFormat.format(value);
    }

    public String getPercentageString(float value) {
        return this.percentageFormat.format(value);
    }
}
