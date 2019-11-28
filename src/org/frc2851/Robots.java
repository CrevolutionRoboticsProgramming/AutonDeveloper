package org.frc2851;

public class Robots
{
    public static Robot hyperion = new Robot("Hyperion", adjustLength(28), adjustLength(48), adjustLength(24), 15, 10, 60);

    public static Robot[] robots = {hyperion};

    private static double adjustLength(double length)
    {
        return length * (3.0 / 2.0);
    }
}
