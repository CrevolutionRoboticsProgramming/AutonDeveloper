package org.frc2851;

import javafx.scene.control.ComboBox;
import org.frc2851.field.Field;
import org.frc2851.field.Fields;

// TODO: Find a less ugly solution
public final class Constants
{
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
