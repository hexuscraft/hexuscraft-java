package net.hexuscraft.core.chat;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class UtilMath {

    public static double trim(double n, int degree, RoundingMode mode) {
        DecimalFormat format = new DecimalFormat("#." + "#".repeat(Math.max(0, degree)));
        format.setRoundingMode(mode);
        return Double.parseDouble(format.format(n));
    }

    public static double trim(double n, int degree) {
        return trim(n, degree, RoundingMode.FLOOR);
    }

    public static double trim(double n, RoundingMode mode) {
        return trim(n, 1, mode);
    }

    public static double trim(double n) {
        return trim(n, 1);
    }

}
