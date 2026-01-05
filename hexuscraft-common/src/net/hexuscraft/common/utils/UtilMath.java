package net.hexuscraft.common.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class UtilMath {

    public static double trim(final double n, final int degree, final RoundingMode mode) {
        final DecimalFormat format = new DecimalFormat("#." + "#".repeat(Math.max(0, degree)));
        format.setRoundingMode(mode);
        return Double.parseDouble(format.format(n));
    }

    public static double trim(final double n, final int degree) {
        return trim(n, degree, RoundingMode.FLOOR);
    }

    public static double trim(final double n, final RoundingMode mode) {
        return trim(n, 1, mode);
    }

    public static double trim(final double n) {
        return trim(n, 1);
    }

}
