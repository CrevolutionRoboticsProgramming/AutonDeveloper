package org.frc2851;

import org.frc2851.field.Field;

public final class Constants
{
    // TODO: Find a solution that's not as ugly as this global variable
    public static Field selectedField;

    public static double pixelsToInches(double pixels)
    {
        return pixels / selectedField.getPixelsToInchesRatio();
    }

    public static double inchesToPixels(double inches)
    {
        return inches * selectedField.getPixelsToInchesRatio();
    }
}
