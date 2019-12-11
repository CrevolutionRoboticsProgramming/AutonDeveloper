package org.frc2851;

public class Robot
{
    public String name;

    // Horizontal
    public double width;

    // Vertical
    public double length;

    // Distance between the center two wheels
    public double wheelbase;

    public int countsPerFoot;

    public double maxVelocity;
    public double maxAcceleration;
    public double maxJerk;

    public Robot(String name, double width, double length, double wheelbase,
                 int countsPerFoot, double maxVelocity, double maxAcceleration, double maxJerk)
    {
        this.name = name;
        this.width = width;
        this.length = length;
        this.wheelbase = wheelbase;
        this.countsPerFoot = countsPerFoot;
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.maxJerk = maxJerk;
    }
}
