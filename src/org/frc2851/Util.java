package org.frc2851;

public final class Util
{
    public static double scaleDimensionUp(double dimension)
    {
        return dimension * (3.0 / 2.0);
    }

    public static double scaleDimensionDown(double dimension)
    {
        return dimension * (2.0 / 3.0);
    }
}
