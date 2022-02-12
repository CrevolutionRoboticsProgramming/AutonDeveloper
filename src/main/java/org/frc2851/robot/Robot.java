package org.frc2851.robot;

import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import org.frc2851.Constants;

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

    /**
     * @param name Name
     * @param width Width in inches
     * @param length Length in inches
     * @param wheelbase Wheelbase in inches
     * @param maxVelocity Max velocity in inches per second
     * @param maxAcceleration Max acceleration in inches per second squared
     */
    public Robot(String name, double width, double length, double wheelbase,
                 double maxVelocity, double maxAcceleration)
    {
        mName = name;
        mWidth = width;
        mLength = length;
        mDriveKinematics = new DifferentialDriveKinematics(wheelbase);
        mMaxVelocity = maxVelocity;
        mMaxAcceleration = maxAcceleration;
    }

    public String getName()
    {
        return mName;
    }
    
    public double getWidthPixels()
    {
        return Constants.inchesToPixels(mWidth);
    }

    public double getWidthInches()
    {
        return mWidth;
    }

    public double getLengthPixels()
    {
        return Constants.inchesToPixels(mLength);
    }

    public double getLengthInches()
    {
        return mLength;
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
}
