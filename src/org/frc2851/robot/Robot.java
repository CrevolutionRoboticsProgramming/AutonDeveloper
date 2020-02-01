package org.frc2851.robot;

import org.frc2851.Util;

public class Robot
{
    private String mName;

    // Horizontal
    private double mWidth;

    // Vertical
    private double mLength;

    // Distance between the center two wheels
    private double mWheelbase;

    private double mMaxVelocity;
    private double mMaxAcceleration;
    private double mMaxJerk;

    /**
     * @param name Name
     * @param width Width in inches
     * @param length Length in inches
     * @param wheelbase Wheelbase in inches
     * @param maxVelocity Max velocity in inches per second
     * @param maxAcceleration Max acceleration in inches per second squared
     * @param maxJerk Max jerk in inches per second cubed
     */
    public Robot(String name, double width, double length, double wheelbase,
                 double maxVelocity, double maxAcceleration, double maxJerk)
    {
        mName = name;
        mWidth = Util.scaleDimensionUp(width);
        mLength = Util.scaleDimensionUp(length);
        mWheelbase = wheelbase;
        mMaxVelocity = maxVelocity;
        mMaxAcceleration = maxAcceleration;
        mMaxJerk = maxJerk;
    }

    public String getName()
    {
        return mName;
    }
    
    public double getWidthPixels()
    {
        return mWidth;
    }

    public double getWidthInches()
    {
        return Util.scaleDimensionDown(mWidth);
    }

    public double getLengthPixels()
    {
        return mLength;
    }

    public double getLengthInches()
    {
        return Util.scaleDimensionDown(mLength);
    }

    public double getWheelbase()
    {
        return mWheelbase;
    }

    public double getMaxVelocity()
    {
        return mMaxVelocity;
    }

    public double getMaxAcceleration()
    {
        return mMaxAcceleration;
    }

    public double getMaxJerk()
    {
        return mMaxJerk;
    }
}
