package org.frc2851.robot;

import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import org.frc2851.Util;

public class Robot
{
    private String mName;

    // Horizontal
    private double mWidth;

    // Vertical
    private double mLength;

    private DifferentialDriveKinematics mDriveKinematics;

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
        mWidth = Util.inchesToPixels(width);
        mLength = Util.inchesToPixels(length);
        mDriveKinematics = new DifferentialDriveKinematics(wheelbase);
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
        return Util.pixelsToInches(mWidth);
    }

    public double getLengthPixels()
    {
        return mLength;
    }

    public double getLengthInches()
    {
        return Util.pixelsToInches(mLength);
    }

    public DifferentialDriveKinematics getDriveKinematics()
    {
        return mDriveKinematics;
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
