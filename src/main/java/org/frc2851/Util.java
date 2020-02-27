package org.frc2851;

public final class Util
{
    public static double inchesToPixels(double inches)
    {
        return inches * (3.0 / 2.0);
    }

    public static double pixelsToInches(double pixels)
    {
        return pixels * (2.0 / 3.0);
    }
}
