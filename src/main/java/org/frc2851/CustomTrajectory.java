package org.frc2851;

import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomTrajectory
{
    private TrajectoryConfig mTrajectoryConfig;
    private ArrayList<Polygon> mArrows;
    private int mStartWaypointIndex;
    private int mEndWaypointIndex;
    private Trajectory mTrajectory;

    public CustomTrajectory(TrajectoryConfig trajectoryConfig, int startWaypointIndex, int endWaypointIndex, Polygon... arrows)
    {
        mTrajectoryConfig = trajectoryConfig;
        mArrows = new ArrayList<>(Arrays.asList(arrows));
        mStartWaypointIndex = startWaypointIndex;
        mEndWaypointIndex = endWaypointIndex;
    }

    public TrajectoryConfig getTrajectoryConfig()
    {
        return mTrajectoryConfig;
    }

    public void setTrajectoryConfig(TrajectoryConfig trajectoryConfig)
    {
        mTrajectoryConfig = trajectoryConfig;
    }

    public ArrayList<Polygon> getArrows()
    {
        return mArrows;
    }

    public void setArrows(List<Polygon> arrows)
    {
        mArrows = new ArrayList<>(arrows);
    }

    public int getStartWaypointIndex()
    {
        return mStartWaypointIndex;
    }

    public void setStartWaypointIndex(int startWaypointIndex)
    {
        mStartWaypointIndex = startWaypointIndex;
    }

    public int getEndWaypointIndex()
    {
        return mEndWaypointIndex;
    }

    public void setEndWaypointIndex(int endWaypointIndex)
    {
        mEndWaypointIndex = endWaypointIndex;
    }

    public Trajectory getTrajectory()
    {
        return mTrajectory;
    }

    public void setTrajectory(Trajectory trajectory)
    {
        mTrajectory = trajectory;
    }
}
