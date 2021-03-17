package org.frc2851;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.frc2851.field.Field;
import org.frc2851.field.Fields;
import org.frc2851.robot.Robot;
import org.frc2851.robot.Robots;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main extends Application
{
    @FXML
    Pane mRoot;
    @FXML
    ImageView mFieldImageView;
    @FXML
    Button mClearNodesButton;
    @FXML
    Button mDeleteNodeButton;
    @FXML
    Button mXSmallMinusButton;
    @FXML
    Button mXLargeMinusButton;
    @FXML
    Button mXSmallPlusButton;
    @FXML
    Button mXLargePlusButton;
    @FXML
    Button mYSmallMinusButton;
    @FXML
    Button mYLargeMinusButton;
    @FXML
    Button mYSmallPlusButton;
    @FXML
    Button mYLargePlusButton;
    @FXML
    Button mExitAngleSmallMinusButton;
    @FXML
    Button mExitAngleLargeMinusButton;
    @FXML
    Button mExitAngleSmallPlusButton;
    @FXML
    Button mExitAngleLargePlusButton;
    @FXML
    Button mSplitJoinPathButton;
    @FXML
    Button mCreateTrajectoryButton;
    @FXML
    Button mExportWaypointsButton;
    @FXML
    Button mImportWaypointsButton;
    @FXML
    Label mWaypointNumberLabel;
    @FXML
    Label mXLabel;
    @FXML
    Label mYLabel;
    @FXML
    Label mExitAngleLabel;
    @FXML
    ComboBox<String> mRobotsComboBox;
    @FXML
    ComboBox<String> mFieldsComboBox;
    @FXML
    Label mPathNumberLabel;
    @FXML
    ComboBox<String> mCustomOptionsComboBox;
    @FXML
    TextField mCustomOptionsTextField;
    @FXML
    Button mStartPauseButton;
    @FXML
    Button mStopButton;
    @FXML
    Button mTimeSmallMinusButton;
    @FXML
    Button mTimeLargeMinusButton;
    @FXML
    Button mTimeSmallPlusButton;
    @FXML
    Button mTimeLargePlusButton;
    @FXML
    Label mTimeLabel;
    @FXML
    Slider mTimeSlider;

    private Stage mStage;

    private Robot mSelectedRobot;

    private ArrayList<DraggableWaypoint> mWaypoints = new ArrayList<>();
    private DraggableWaypoint mSelectedWaypoint;
    private ArrayList<CustomTrajectory> mCustomTrajectories = new ArrayList<>();
    private CustomTrajectory mSelectedTrajectory;

    private ArrayList<Polygon> mProjectedPath = new ArrayList<>();

    private int mCurrentPlaybackTimeMs = 0;
    private int mDeltaTMs = 10;

    private RobotRepresentation mRobotRepresentation;
    private TrajectoryFollowerState mTrajectoryFollowerState = TrajectoryFollowerState.STOPPED;

    // The order of the entries is arbitrary and doesn't affect how they're ordered in the program
    private HashMap<String, String> mCustomOptionsHashMap = new HashMap<>(
            Map.of("Start Velocity", String.valueOf(0.0),
                    "End Velocity", String.valueOf(0.0),
                    "Reversed", String.valueOf(false))
    );

    @FXML
    private void initialize()
    {
        for (Field field : Fields.fields)
        {
            mFieldsComboBox.getItems().add(field.getName());
        }
        mFieldsComboBox.getSelectionModel().selectFirst();

        mFieldsComboBox.setOnAction(event ->
        {
            // Sets the image displayed in the field ImageView to the newly selected field
            mFieldImageView.setImage(new Image(Fields.fields[mFieldsComboBox.getSelectionModel().getSelectedIndex()].getPictureUrl()));
            Constants.selectedField = Fields.fields[mFieldsComboBox.getSelectionModel().getSelectedIndex()];

            if (mRobotRepresentation != null)
                mRobotRepresentation.setWidthHeight(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels());

            mClearNodesButton.fire();
        });

        // Sets default to first field
        mFieldsComboBox.getOnAction().handle(new ActionEvent());

        for (Robot robot : Robots.robots)
        {
            mRobotsComboBox.getItems().add(robot.getName());
        }
        mRobotsComboBox.getSelectionModel().selectFirst();

        // Sets default to first robot
        mSelectedRobot = Robots.robots[0];

        mRobotRepresentation = new RobotRepresentation(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels());
        mRobotRepresentation.setLayoutX(mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX());
        mRobotRepresentation.setLayoutY(mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY());
        mRobotRepresentation.setTranslateX(-1000);
        mRobotRepresentation.setTranslateY(-1000);
        mRoot.getChildren().add(mRobotRepresentation);
        mRobotsComboBox.setOnAction(event ->
        {
            // Sets the selected robot to the newly selected robot
            mSelectedRobot = Robots.robots[mRobotsComboBox.getSelectionModel().getSelectedIndex()];
            mRobotRepresentation.setWidthHeight(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels());
        });

        mCustomOptionsComboBox.getItems().addAll(mCustomOptionsHashMap.keySet());
        mCustomOptionsComboBox.setOnAction(event ->
        {
            mCustomOptionsHashMap.replace("Start Velocity", String.valueOf(mSelectedTrajectory.getTrajectoryConfig().getStartVelocity()));
            mCustomOptionsHashMap.replace("End Velocity", String.valueOf(mSelectedTrajectory.getTrajectoryConfig().getEndVelocity()));
            mCustomOptionsHashMap.replace("Reversed", String.valueOf(mSelectedTrajectory.getTrajectoryConfig().isReversed()));
            mCustomOptionsTextField.setText(mCustomOptionsHashMap.get(mCustomOptionsComboBox.getSelectionModel().getSelectedItem()));
        });
        mCustomOptionsTextField.setOnKeyReleased(event ->
        {
            mCustomOptionsHashMap.replace(mCustomOptionsComboBox.getSelectionModel().getSelectedItem(), mCustomOptionsTextField.getText());
            mSelectedTrajectory.setTrajectoryConfig(mSelectedTrajectory.getTrajectoryConfig()
                    .setStartVelocity(Double.parseDouble(mCustomOptionsHashMap.get("Start Velocity")))
                    .setEndVelocity(Double.parseDouble(mCustomOptionsHashMap.get("End Velocity")))
                    .setReversed(Boolean.parseBoolean(mCustomOptionsHashMap.get("Reversed"))));
            mSelectedTrajectory.generateTrajectory();
            updateProjectedPath();
        });

        mFieldImageView.setOnMousePressed(event ->
        {
            mStopButton.fire();

            addNode(event.getX(), event.getY(), 0.0);
        });

        mClearNodesButton.setOnAction(event ->
        {
            stopTrajectoryFollower();
            mRoot.getChildren().removeAll(mWaypoints);
            mWaypoints.clear();
            mCustomTrajectories.clear();
            updateProjectedPath();
            updateWaypointDisplay(null);
            updatePathDisplay(null);
        });

        mDeleteNodeButton.setOnAction(event ->
        {
            stopTrajectoryFollower();
            if (mSelectedWaypoint != null)
            {
                for (int i = mWaypoints.indexOf(mSelectedWaypoint) + 1; i < mWaypoints.size(); ++i)
                    mWaypoints.get(i).setRank(i);

                if (!mCustomTrajectories.isEmpty())
                {
                    if (mCustomTrajectories.get(mCustomTrajectories.size() - 1).getStartWaypointIndex() == mWaypoints.indexOf(mSelectedWaypoint) - 1)
                        mCustomTrajectories.remove(mCustomTrajectories.size() - 1);
                    else
                        mCustomTrajectories.get(mCustomTrajectories.size() - 1).setEndWaypointIndex(mWaypoints.indexOf(mSelectedWaypoint) - 1);
                }

                mRoot.getChildren().remove(mSelectedWaypoint);
                mWaypoints.remove(mSelectedWaypoint);

                if (!mCustomTrajectories.isEmpty())
                    updatePathDisplay(mCustomTrajectories.get(mCustomTrajectories.size() - 1));
                else
                    updatePathDisplay(null);

                updateWaypointDisplay(null);
                updateTrajectoriesToFollow();
                updateProjectedPath();
            }
        });

        mSplitJoinPathButton.setOnAction(event ->
        {
            if (mWaypoints.indexOf(mSelectedWaypoint) == 0 || mWaypoints.indexOf(mSelectedWaypoint) == mWaypoints.size() - 1)
                return;

            if (mSplitJoinPathButton.getText().equals("Split Path"))
            {
                int lastEndingWaypointBeforeSelectedWaypointIndex = 0;
                int lastTrajectoryIndex = 0;
                boolean endingWaypointBreakFlag = false;
                // Searching backwards from the selected waypoint for an ending waypoint
                for (int i = mWaypoints.indexOf(mSelectedWaypoint) - 1; i > 0; --i)
                {
                    if (endingWaypointBreakFlag)
                        break;
                    for (CustomTrajectory trajectory : mCustomTrajectories)
                    {
                        if (trajectory.getEndWaypointIndex() == i)
                        {
                            lastEndingWaypointBeforeSelectedWaypointIndex = i;
                            lastTrajectoryIndex = mCustomTrajectories.indexOf(trajectory);
                            endingWaypointBreakFlag = true;
                            break;
                        }
                    }
                }

                int firstStartingWaypointAfterSelectedWaypointIndex = mWaypoints.size() - 1;
                boolean startingWaypointBreakFlag = false;
                // Searching forwards from the selected waypoint for a starting waypoint
                for (int i = mWaypoints.indexOf(mSelectedWaypoint) + 1; i < mWaypoints.size(); ++i)
                {
                    if (startingWaypointBreakFlag)
                        break;
                    for (CustomTrajectory trajectory : mCustomTrajectories)
                    {
                        if (trajectory.getStartWaypointIndex() == i)
                        {
                            firstStartingWaypointAfterSelectedWaypointIndex = i;
                            startingWaypointBreakFlag = true;
                            break;
                        }
                    }
                }

                /*
                Add a new trajectory going from the last end (or zero) to the site of the split
                Add a new trajectory going from the site of the split to the next start (or the last waypoint)
                Delete the trajectory which started at the last end (or zero) and ended at the next start (or the last waypoint)
                 */

                // If the selected waypoint is between the start and end of the last trajectory, carry the properties
                // of the last trajectory into the first half of the split; otherwise, don't

                TrajectoryConfig configToCarryOver;
                if (mWaypoints.indexOf(mSelectedWaypoint) > mCustomTrajectories.get(lastTrajectoryIndex).getStartWaypointIndex()
                        && mWaypoints.indexOf(mSelectedWaypoint) < mCustomTrajectories.get(lastTrajectoryIndex).getEndWaypointIndex())
                    configToCarryOver = mCustomTrajectories.get(lastTrajectoryIndex).getTrajectoryConfig();
                else
                    configToCarryOver = getTrajectoryConfig();

                mCustomTrajectories.add(lastTrajectoryIndex + 1,
                        new CustomTrajectory(
                                configToCarryOver,//mCustomTrajectories.get(lastTrajectoryIndex).getTrajectoryConfig(),
                                lastEndingWaypointBeforeSelectedWaypointIndex,
                                mWaypoints.indexOf(mSelectedWaypoint)));

                mCustomTrajectories.add(lastTrajectoryIndex + 2,
                        new CustomTrajectory(
                                getTrajectoryConfig(),
                                mWaypoints.indexOf(mSelectedWaypoint),
                                firstStartingWaypointAfterSelectedWaypointIndex));

                Iterator<CustomTrajectory> trajectoryIterator = mCustomTrajectories.iterator();
                while (trajectoryIterator.hasNext())
                {
                    CustomTrajectory trajectory = trajectoryIterator.next();
                    if (trajectory.getStartWaypointIndex() == lastEndingWaypointBeforeSelectedWaypointIndex
                            && trajectory.getEndWaypointIndex() == firstStartingWaypointAfterSelectedWaypointIndex)
                        trajectoryIterator.remove();
                }

                // If we haven't yet selected a trajectory, don't change the selection
                if (mSelectedTrajectory != null)
                    mSelectedTrajectory = mCustomTrajectories.get(lastTrajectoryIndex);

                updateProjectedPath();

                mSplitJoinPathButton.setText("Join Path");
            } else
            {
                if (mWaypoints.indexOf(mSelectedWaypoint) == 0 || mWaypoints.indexOf(mSelectedWaypoint) == mWaypoints.size() - 1)
                    return;

                int firstTrajectoryIndex = 0, secondTrajectoryIndex = 1;
                for (CustomTrajectory trajectory : mCustomTrajectories)
                {
                    if (trajectory.getEndWaypointIndex() == mWaypoints.indexOf(mSelectedWaypoint))
                        firstTrajectoryIndex = mCustomTrajectories.indexOf(trajectory);
                    if (trajectory.getStartWaypointIndex() == mWaypoints.indexOf(mSelectedWaypoint))
                        secondTrajectoryIndex = mCustomTrajectories.indexOf(trajectory);
                }

                mCustomTrajectories.get(firstTrajectoryIndex).setEndWaypointIndex(secondTrajectoryIndex);
                mCustomTrajectories.get(firstTrajectoryIndex).getArrows().addAll(mCustomTrajectories.get(secondTrajectoryIndex).getArrows());
                mCustomTrajectories.remove(secondTrajectoryIndex);

                mSplitJoinPathButton.setText("Split Path");
            }

            updateArrowOwnership();
        });

        mCreateTrajectoryButton.setOnAction(event ->
        {
            ArrayList<Trajectory> generatedTrajectories = new ArrayList<>();

            for (CustomTrajectory trajectory : mCustomTrajectories)
            {
                ArrayList<Pose2d> poses = new ArrayList<>();
                for (int i = trajectory.getStartWaypointIndex(); i <= trajectory.getEndWaypointIndex(); ++i)
                {
                    double angle = -mWaypoints.get(i).getRotate() + 90;
                    if (Boolean.parseBoolean(mCustomOptionsHashMap.get("Reversed")))
                        angle += 180;
                    poses.add(new Pose2d(mWaypoints.get(i).getXInches(), mWaypoints.get(i).getFlippedYInches(), Rotation2d.fromDegrees(angle)));
                }

                /*
                double inchesToMeters = 0.0254;

                StringBuilder trajectoryStringBuilder = new StringBuilder("Trajectory newTrajectory = TrajectoryGenerator.generateTrajectory(\n" +
                        "new Pose2d(" + (poses.get(0).getTranslation().getX() * inchesToMeters) + ", "
                         + (poses.get(0).getTranslation().getY() * inchesToMeters)
                         + ", new Rotation2d(Math.toRadians("
                         + Math.toDegrees(poses.get(0).getRotation().getRadians()) + "))),\n" +
                        "List.of(\n");

                for (int i = 1; i < poses.size() - 1; ++i)
                {
                    trajectoryStringBuilder.append("new Translation2d(")
                    .append(poses.get(i).getTranslation().getX() * inchesToMeters)
                    .append(", ").append(poses.get(i).getTranslation().getY() * inchesToMeters).append(")");
                    if (i != poses.size() - 2)
                        trajectoryStringBuilder.append(",");
                    trajectoryStringBuilder.append("\n");
                }

                trajectoryStringBuilder.append("),\n")
                        .append("new Pose2d(")
                        .append(poses.get(poses.size() - 1).getTranslation().getX() * inchesToMeters)
                        .append(", ").append(poses.get(poses.size() - 1).getTranslation().getY() * inchesToMeters)
                        .append(", new Rotation2d(Math.toRadians(")
                        .append(Math.toDegrees(poses.get(poses.size() - 1).getRotation().getRadians()))
                        .append("))),\n")
                        .append("config\n")
                        .append(");");

                System.out.println(trajectoryStringBuilder);
                */

                generatedTrajectories.add(TrajectoryGenerator.generateTrajectory(poses, trajectory.getTrajectoryConfig()));
            }

            try
            {
                FileWriter leftTrajectoryFileWriter = new FileWriter("LeftTrajectory.csv");
                FileWriter rightTrajectoryFileWriter = new FileWriter("RightTrajectory.csv");
                leftTrajectoryFileWriter.append("dt,x,y,position,velocity,acceleration,jerk,heading\n");
                rightTrajectoryFileWriter.append("dt,x,y,position,velocity,acceleration,jerk,heading\n");

                for (Trajectory trajectory : generatedTrajectories)
                {
                    double lastLeftVelocity = 0.0;
                    double lastLeftAcceleration = 0.0;
                    double leftPosition = 0.0;

                    double lastRightVelocity = 0.0;
                    double lastRightAcceleration = 0.0;
                    double rightPosition = 0.0;

                    double leftVelocity, rightVelocity;
                    double leftAcceleration, rightAcceleration;
                    double leftJerk, rightJerk;

                    DifferentialDriveWheelSpeeds chassisSpeeds;
                    for (int timeMs = 0; timeMs < trajectory.getTotalTimeSeconds() * 1000; ++timeMs)
                    {
                        Trajectory.State state = trajectory.sample(timeMs / 1000.0);

                        chassisSpeeds = mSelectedRobot.getDriveKinematics().toWheelSpeeds(
                                new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.velocityMetersPerSecond * state.curvatureRadPerMeter));

                        leftVelocity = chassisSpeeds.leftMetersPerSecond;
                        leftAcceleration = (leftVelocity - lastLeftVelocity) * 1000;
                        leftJerk = (leftAcceleration - lastLeftAcceleration) * 1000;
                        leftTrajectoryFileWriter.append(String.valueOf(1.0))
                                .append(",").append(String.valueOf(state.poseMeters.getTranslation().getX() - (state.poseMeters.getRotation().getSin() * mSelectedRobot.getWidthInches() / 2)))
                                .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY() + (state.poseMeters.getRotation().getCos() * mSelectedRobot.getLengthInches() / 2)))
                                .append(",").append(String.valueOf(leftPosition))
                                .append(",").append(String.valueOf(leftVelocity))
                                .append(",").append(String.valueOf(leftAcceleration))
                                .append(",").append(String.valueOf(leftJerk))
                                .append(",").append(String.valueOf(state.poseMeters.getRotation().getRadians()))
                                .append('\n');
                        leftPosition += leftVelocity / 1000.0;
                        lastLeftVelocity = leftVelocity;
                        lastLeftAcceleration = leftAcceleration;

                        rightVelocity = chassisSpeeds.rightMetersPerSecond;
                        rightAcceleration = (rightVelocity - lastRightVelocity) * 1000;
                        rightJerk = (rightAcceleration - lastRightAcceleration) * 1000;
                        rightTrajectoryFileWriter.append(String.valueOf(1.0))
                                .append(",").append(String.valueOf(state.poseMeters.getTranslation().getX() + (state.poseMeters.getRotation().getSin() * mSelectedRobot.getWidthInches() / 2)))
                                .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY() - (state.poseMeters.getRotation().getCos() * mSelectedRobot.getLengthInches() / 2)))
                                .append(",").append(String.valueOf(rightPosition))
                                .append(",").append(String.valueOf(rightVelocity))
                                .append(",").append(String.valueOf(rightAcceleration))
                                .append(",").append(String.valueOf(rightJerk))
                                .append(",").append(String.valueOf(state.poseMeters.getRotation().getRadians()))
                                .append('\n');
                        rightPosition += rightVelocity / 1000.0;
                        lastRightVelocity = rightVelocity;
                        lastRightAcceleration = rightAcceleration;
                    }
                }
                leftTrajectoryFileWriter.close();
                rightTrajectoryFileWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        mExportWaypointsButton.setOnAction(event ->
        {
            try
            {
                FileWriter waypointsFileWriter = new FileWriter("Waypoints.csv");

                waypointsFileWriter.append("x,y,heading\n");
                for (DraggableWaypoint waypoint : mWaypoints)
                {
                    waypointsFileWriter
                            .append(String.valueOf(waypoint.getXInches()))
                            .append(",").append(String.valueOf(waypoint.getFlippedYInches()))
                            .append(",").append(String.valueOf(waypoint.getRotate()))
                            .append('\n');
                }

                waypointsFileWriter.append("startIndex,endIndex,startVelocity,endVelocity,isReversed\n");
                for (CustomTrajectory customTrajectory : mCustomTrajectories)
                {
                    waypointsFileWriter
                            .append(String.valueOf(customTrajectory.getStartWaypointIndex()))
                            .append(",").append(String.valueOf(customTrajectory.getEndWaypointIndex()))
                            .append(",").append(String.valueOf(customTrajectory.getTrajectoryConfig().getStartVelocity()))
                            .append(",").append(String.valueOf(customTrajectory.getTrajectoryConfig().getEndVelocity()))
                            .append(",").append(String.valueOf(customTrajectory.getTrajectoryConfig().isReversed()))
                            .append('\n');
                }

                waypointsFileWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        mImportWaypointsButton.setOnAction(event ->
        {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(mStage);
            if (file != null)
            {
                mClearNodesButton.fire();

                try
                {
                    BufferedReader waypointsBufferedReader = new BufferedReader(new FileReader(file));

                    // TODO: Make a prettier solution, probably with YAML
                    boolean readingWaypoints = true;

                    // Discards the waypoints header
                    String line = waypointsBufferedReader.readLine();
                    String[] properties;
                    while ((line = waypointsBufferedReader.readLine()) != null)
                    {
                        properties = line.split(",");

                        if (properties[0].equals("startIndex"))
                        {
                            readingWaypoints = false;
                            mCustomTrajectories.clear();
                            configureButtons(mWaypoints.get(mWaypoints.size() - 1));
                            continue;
                        }

                        if (readingWaypoints)
                        {
                            addNode(Constants.inchesToPixels(Double.parseDouble(properties[0])),
                                    Constants.inchesToPixels(Constants.selectedField.getWidth() - Double.parseDouble(properties[1])),
                                    Double.parseDouble(properties[2]));
                        } else
                        {
                            TrajectoryConfig config = getTrajectoryConfig()
                                    .setStartVelocity(Double.parseDouble(properties[2]))
                                    .setEndVelocity(Double.parseDouble(properties[3]))
                                    .setReversed(Boolean.parseBoolean(properties[4]));
                            mCustomTrajectories.add(new CustomTrajectory(config, Integer.parseInt(properties[0]), Integer.parseInt(properties[1])));
                        }
                    }
                    waypointsBufferedReader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            updateProjectedPath();
        });

        final Timeline lineDrawer = new Timeline(
                new KeyFrame(Duration.ZERO, event ->
                        updateProjectedPath()),
                new KeyFrame(Duration.millis(25))
        );
        lineDrawer.setCycleCount(Timeline.INDEFINITE);

        mRoot.setOnMousePressed(event -> lineDrawer.play());
        mRoot.setOnMouseReleased(event -> lineDrawer.stop());

        final Timeline trajectoryFollower = new Timeline(
                new KeyFrame(Duration.ZERO, event ->
                {
                    if (mTrajectoryFollowerState == TrajectoryFollowerState.RUNNING)
                    {
                        double totalTimeSeconds = 0;
                        for (CustomTrajectory customTrajectory : mCustomTrajectories)
                            totalTimeSeconds += customTrajectory.getTrajectory().getTotalTimeSeconds();

                        if (mCurrentPlaybackTimeMs < totalTimeSeconds * 1000)
                        {
                            updateTrajectoryFollowerDisplay();
                            advanceTrajectory();
                            mCurrentPlaybackTimeMs += mDeltaTMs;
                        } else
                            stopTrajectoryFollower();
                    }
                }),
                new KeyFrame(Duration.millis(mDeltaTMs))
        );
        trajectoryFollower.setCycleCount(Timeline.INDEFINITE);
        trajectoryFollower.play();

        mStartPauseButton.setOnAction((event) ->
        {
            switch (mTrajectoryFollowerState)
            {
                case STOPPED:
                    mStartPauseButton.setText("Pause");
                    startTrajectoryFollower();
                    break;
                case RUNNING:
                    mStartPauseButton.setText("Start");
                    mTrajectoryFollowerState = TrajectoryFollowerState.PAUSED;
                    break;
                case PAUSED:
                    mStartPauseButton.setText("Pause");
                    mTrajectoryFollowerState = TrajectoryFollowerState.RUNNING;
                    break;
            }
        });

        mStopButton.setOnAction((event) -> stopTrajectoryFollower());

        mTimeSlider.setOnMouseDragged((event) ->
        {
            if (mTrajectoryFollowerState == TrajectoryFollowerState.STOPPED)
            {
                mStartPauseButton.fire();
                mStartPauseButton.fire();
            }

            mCurrentPlaybackTimeMs = (int) (mTimeSlider.getValue() * 1000);
            updateTrajectoryFollowerDisplay();
            advanceTrajectory();
        });

        mTimeSmallMinusButton.setOnAction((event) ->
        {
            mCurrentPlaybackTimeMs -= 10;
            updateTrajectoryFollowerDisplay();
            advanceTrajectory();
        });
        mTimeLargeMinusButton.setOnAction((event) ->
        {
            mCurrentPlaybackTimeMs -= 250;
            updateTrajectoryFollowerDisplay();
            advanceTrajectory();
        });
        mTimeSmallPlusButton.setOnAction((event) ->
        {
            mCurrentPlaybackTimeMs += 10;
            updateTrajectoryFollowerDisplay();
            advanceTrajectory();
        });
        mTimeLargePlusButton.setOnAction((event) ->
        {
            mCurrentPlaybackTimeMs += 250;
            updateTrajectoryFollowerDisplay();
            advanceTrajectory();
        });

        updateWaypointDisplay(null);
    }

    private void addNode(double x, double y, double rotation)
    {
        DraggableWaypoint newWaypoint = new DraggableWaypoint(
                mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels(),
                mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX(),
                mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY(),
                x - mSelectedRobot.getWidthPixels() / 2, y - mSelectedRobot.getLengthPixels() / 2,
                mWaypoints.size() + 1);

        newWaypoint.setRotate(rotation);

        newWaypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());

        Runnable selectThisWaypoint = () ->
        {
            mStopButton.fire();
            mSelectedWaypoint = newWaypoint;
            updateWaypointDisplay(mSelectedWaypoint);
            configureButtons(newWaypoint);

            boolean trajectorySplitsOnWaypoint = false;
            for (CustomTrajectory trajectory : mCustomTrajectories)
            {
                if (trajectory.getEndWaypointIndex() == mWaypoints.indexOf(mSelectedWaypoint) && mCustomTrajectories.indexOf(trajectory) != mCustomTrajectories.size() - 1)
                {
                    trajectorySplitsOnWaypoint = true;
                    break;
                }
            }

            mSplitJoinPathButton.setText(trajectorySplitsOnWaypoint ? "Join Path" : "Split Path");
        };

        newWaypoint.setOnMousePressed(mousePressedEvent ->
                selectThisWaypoint.run());

        newWaypoint.mRectangle.setOnMouseDragged(mouseDraggedEvent ->
        {
            selectThisWaypoint.run();
            newWaypoint.followMouse(mouseDraggedEvent);
            newWaypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
        });

        newWaypoint.mRankLabel.setOnMouseDragged(mouseDraggedEvent ->
        {
            selectThisWaypoint.run();
            newWaypoint.followMouse(mouseDraggedEvent);
            newWaypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
        });

        newWaypoint.mRotateIcon.setOnMouseDragged(mouseDraggedEvent ->
        {
            selectThisWaypoint.run();
            newWaypoint.setRotate(90 - Math.toDegrees(Math.atan2(
                    newWaypoint.getDimensions().center.getY() - mouseDraggedEvent.getSceneY() - (mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY())
                            + (newWaypoint.getHeight() / 2),
                    mouseDraggedEvent.getSceneX() - newWaypoint.getDimensions().center.getX() - (mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX()))));
            newWaypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
        });

        mWaypoints.add(newWaypoint);
        mRoot.getChildren().add(newWaypoint);

        selectThisWaypoint.run();

        updateTrajectoriesToFollow();
        updateProjectedPath();

        if (mWaypoints.size() == 2)
        {
            CustomTrajectory customTrajectory = new CustomTrajectory(getTrajectoryConfig(), 0, 1, mProjectedPath.toArray(new Polygon[0]));
            customTrajectory.setPoses(getPoses(customTrajectory));
            customTrajectory.generateTrajectory();
            mCustomTrajectories.add(customTrajectory);
        }
        if (mWaypoints.size() > 2)
            mCustomTrajectories.get(mCustomTrajectories.size() - 1).setEndWaypointIndex(mWaypoints.size() - 1);
    }

    private void updateArrowOwnership()
    {
        if (mCustomTrajectories.size() == 1)
            mCustomTrajectories.get(0).setArrows(mProjectedPath);

        if (mCustomTrajectories.size() > 1)
        {
            ArrayList<Integer> endArrowIndices = new ArrayList<>();

            if (!mProjectedPath.isEmpty())
            {
                // TODO: When an arrow later in the path is closer to the waypoint than one earlier in the path, how is it prioritized?
                for (CustomTrajectory trajectory : mCustomTrajectories)
                {
                    double smallestDistance = Double.POSITIVE_INFINITY;
                    int closestArrowIndex = 0;
                    for (int arrowIndex = 0; arrowIndex < mProjectedPath.size(); ++arrowIndex)
                    {
                        double xDifference = Math.abs(Constants.pixelsToInches(mProjectedPath.get(arrowIndex).getTranslateX()) - mWaypoints.get(trajectory.getEndWaypointIndex()).getXInches());
                        double yDifference = Math.abs(Constants.pixelsToInches(mProjectedPath.get(arrowIndex).getTranslateY()) - mWaypoints.get(trajectory.getEndWaypointIndex()).getUnflippedYInches());
                        double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference);
                        if (distance < smallestDistance)
                        {
                            smallestDistance = distance;
                            closestArrowIndex = arrowIndex;
                        }
                    }
                    endArrowIndices.add(closestArrowIndex);
                }
            }

            if (!endArrowIndices.isEmpty())
            {
                mCustomTrajectories.get(0).setArrows(mProjectedPath.subList(0, endArrowIndices.get(0)));

                // Set the arrows to the arrows ranging from the previous trajectory's last arrow to the arrow we found
                for (int i = 1; i < endArrowIndices.size(); ++i)
                {
                    mCustomTrajectories.get(i).setArrows(mProjectedPath.subList(
                            mProjectedPath.indexOf(mCustomTrajectories.get(i - 1).getArrows().get(mCustomTrajectories.get(i - 1).getArrows().size() - 1)) + 1,
                            endArrowIndices.get(i)));
                }
            }
        }

        for (int arrowIndex = 0; arrowIndex < mProjectedPath.size(); ++arrowIndex)
        {
            int finalArrowIndex = arrowIndex;

            mProjectedPath.get(arrowIndex).setOnMousePressed(mousePressedEvent ->
            {
                for (int trajectoryIndex = 0; trajectoryIndex < mCustomTrajectories.size(); ++trajectoryIndex)
                {
                    if (mCustomTrajectories.get(trajectoryIndex).getArrows().contains(mProjectedPath.get(finalArrowIndex)))
                    {
                        mSelectedTrajectory = mCustomTrajectories.get(trajectoryIndex);
                        for (Polygon arrowToHighlight : mCustomTrajectories.get(trajectoryIndex).getArrows())
                            arrowToHighlight.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.valueOf("GREEN"), 10, 1, 0, 0));
                        updatePathDisplay(mSelectedTrajectory);
                    } else
                    {
                        for (Polygon arrowToUnhighlight : mCustomTrajectories.get(trajectoryIndex).getArrows())
                            arrowToUnhighlight.setEffect(null);
                    }
                }
            });
        }
    }

    private void updateWaypointDisplay(DraggableWaypoint waypoint)
    {
        if (waypoint == null)
        {
            mWaypointNumberLabel.setText("");
            mXLabel.setText("");
            mYLabel.setText("");
            mExitAngleLabel.setText("");
        } else
        {
            DecimalFormat formatter = new DecimalFormat("000.00");
            mWaypointNumberLabel.setText(String.valueOf(mWaypoints.indexOf(waypoint) + 1));
            mXLabel.setText(formatter.format(waypoint.getXInches()));
            mYLabel.setText(formatter.format(waypoint.getFlippedYInches()));
            mExitAngleLabel.setText(formatter.format(waypoint.getRotate()));
        }
    }

    private void updatePathDisplay(CustomTrajectory customTrajectory)
    {
        if (customTrajectory == null)
        {
            mPathNumberLabel.setText("");
            mCustomOptionsComboBox.getSelectionModel().clearSelection();
            mCustomOptionsTextField.clear();
            mCustomOptionsComboBox.setDisable(true);
            mCustomOptionsTextField.setDisable(true);
        } else
        {
            mPathNumberLabel.setText(String.valueOf(mCustomTrajectories.indexOf(customTrajectory) + 1));
            mCustomOptionsComboBox.getSelectionModel().selectFirst();
            mCustomOptionsTextField.setText(mCustomOptionsHashMap.get(mCustomOptionsComboBox.getSelectionModel().getSelectedItem()));
            mCustomOptionsHashMap.replace("Reversed", String.valueOf(customTrajectory.getTrajectoryConfig().isReversed()));
            mCustomOptionsHashMap.replace("Start Velocity", String.valueOf(customTrajectory.getTrajectoryConfig().getStartVelocity()));
            mCustomOptionsHashMap.replace("End Velocity", String.valueOf(customTrajectory.getTrajectoryConfig().getEndVelocity()));
            mCustomOptionsComboBox.setDisable(false);
            mCustomOptionsTextField.setDisable(false);
            mCustomOptionsComboBox.getOnAction().handle(new ActionEvent());
        }
    }

    private void startTrajectoryFollower()
    {
        mTimeSlider.setMax(getAllTrajectoriesTotalTimeSeconds());
        mTrajectoryFollowerState = TrajectoryFollowerState.RUNNING;
    }

    private void stopTrajectoryFollower()
    {
        mCurrentPlaybackTimeMs = 0;
        mTimeSlider.setMax(1);
        mStartPauseButton.setText("Start");
        mRobotRepresentation.setTranslateX(-1000);
        mRobotRepresentation.setTranslateY(-1000);
        updateTrajectoryFollowerDisplay();
        mTrajectoryFollowerState = TrajectoryFollowerState.STOPPED;
    }

    private void configureButtons(DraggableWaypoint waypoint)
    {
        double smallOffset = Constants.inchesToPixels(0.25);
        double largeOffset = Constants.inchesToPixels(1.0);

        Runnable runnable = () ->
        {
            mStopButton.fire();
            waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
            waypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
            updateWaypointDisplay(mSelectedWaypoint);
            updateTrajectoriesToFollow();
            updateProjectedPath();
        };

        configureLongPressButton(mXSmallMinusButton, () ->
        {
            waypoint.setTranslateX(waypoint.getTranslateX() - smallOffset);
            runnable.run();
        });
        configureLongPressButton(mXLargeMinusButton, () ->
        {
            waypoint.setTranslateX(waypoint.getTranslateX() - largeOffset);
            runnable.run();
        });
        configureLongPressButton(mXSmallPlusButton, () ->
        {
            waypoint.setTranslateX(waypoint.getTranslateX() + smallOffset);
            runnable.run();
        });
        configureLongPressButton(mXLargePlusButton, () ->
        {
            waypoint.setTranslateX(waypoint.getTranslateX() + largeOffset);
            runnable.run();
        });

        configureLongPressButton(mYSmallMinusButton, () ->
        {
            waypoint.setTranslateY(waypoint.getTranslateY() + smallOffset);
            runnable.run();
        });
        configureLongPressButton(mYLargeMinusButton, () ->
        {
            waypoint.setTranslateY(waypoint.getTranslateY() + largeOffset);
            runnable.run();
        });
        configureLongPressButton(mYSmallPlusButton, () ->
        {
            waypoint.setTranslateY(waypoint.getTranslateY() - smallOffset);
            runnable.run();
        });
        configureLongPressButton(mYLargePlusButton, () ->
        {
            waypoint.setTranslateY(waypoint.getTranslateY() - largeOffset);
            runnable.run();
        });

        configureLongPressButton(mExitAngleSmallMinusButton, () ->
        {
            waypoint.setRotate(waypoint.getRotate() - 0.25);
            runnable.run();
        });
        configureLongPressButton(mExitAngleLargeMinusButton, () ->
        {
            waypoint.setRotate(waypoint.getRotate() - 1);
            runnable.run();
        });
        configureLongPressButton(mExitAngleSmallPlusButton, () ->
        {
            waypoint.setRotate(waypoint.getRotate() + 0.25);
            runnable.run();
        });
        configureLongPressButton(mExitAngleLargePlusButton, () ->
        {
            waypoint.setRotate(waypoint.getRotate() + 1);
            runnable.run();
        });
    }

    private void configureLongPressButton(Button button, Runnable onClick)
    {
        final AnimationTimer timer = new AnimationTimer()
        {
            private long mBegin = 0;

            @Override
            public void start()
            {
                mBegin = System.currentTimeMillis();
                super.start();
            }

            @Override
            public void handle(long time)
            {
                if (System.currentTimeMillis() - mBegin > 500
                        && System.currentTimeMillis() % 100 < 50)
                    onClick.run();
            }
        };

        button.setOnMousePressed(event ->
        {
            onClick.run();
            timer.start();
        });

        button.setOnMouseReleased(event -> timer.stop());
    }

    private void updateTrajectoryFollowerDisplay()
    {
        DecimalFormat formatter = new DecimalFormat("00.000");
        mTimeLabel.setText(formatter.format(mCurrentPlaybackTimeMs / 1000.0));
        mTimeSlider.setValue(mCurrentPlaybackTimeMs / 1000.0);
    }

    private void advanceTrajectory()
    {
        double lastTimeMs, totalTimeMs = 0;
        for (CustomTrajectory customTrajectory : mCustomTrajectories)
        {
            lastTimeMs = totalTimeMs;
            totalTimeMs += customTrajectory.getTrajectory().getTotalTimeSeconds() * 1000;
            if (mCurrentPlaybackTimeMs > lastTimeMs
                    && mCurrentPlaybackTimeMs < totalTimeMs)
            {
                Trajectory.State state = customTrajectory.getTrajectory().sample((mCurrentPlaybackTimeMs - (totalTimeMs - customTrajectory.getTrajectory().getTotalTimeSeconds() * 1000)) / 1000.0);
                mRobotRepresentation.setTranslateX(Constants.inchesToPixels(state.poseMeters.getTranslation().getX()) - mSelectedRobot.getWidthPixels() / 2);
                mRobotRepresentation.setTranslateY(Constants.inchesToPixels(state.poseMeters.getTranslation().getY()) - mSelectedRobot.getLengthPixels() / 2);
                mRobotRepresentation.setRotate(state.poseMeters.getRotation().getDegrees() + 90);

                mRobotRepresentation.toFront();
                break;
            }
        }
    }

    private void updateTrajectoriesToFollow()
    {
        if (mWaypoints.size() < 2)
            return;

        for (CustomTrajectory customTrajectory : mCustomTrajectories)
        {
            customTrajectory.setPoses(getPoses(customTrajectory));
            customTrajectory.generateTrajectory();
        }
    }

    private ArrayList<Pose2d> getPoses(CustomTrajectory customTrajectory)
    {
        ArrayList<Pose2d> poses = new ArrayList<>();
        for (int i = customTrajectory.getStartWaypointIndex(); i <= customTrajectory.getEndWaypointIndex(); ++i)
        {
            double angle = mWaypoints.get(i).getRotate() - 90;
            poses.add(new Pose2d(
                    Constants.pixelsToInches(mWaypoints.get(i).getDimensions().center.getX()),
                    Constants.pixelsToInches(mWaypoints.get(i).getDimensions().center.getY()),
                    Rotation2d.fromDegrees(angle)));
        }
        return poses;
    }

    private void updateProjectedPath()
    {
        for (Polygon arrow : mProjectedPath)
            mRoot.getChildren().remove(arrow);

        mProjectedPath.clear();

        if (mWaypoints.size() >= 2)
        {
            updateTrajectoriesToFollow();

            // Time which passes during robot moving over the height of the triangle
            double preHeightOfTriangle = mSelectedRobot.getLengthPixels() / 4;
            double dt = preHeightOfTriangle / Constants.inchesToPixels(mSelectedRobot.getMaxVelocity() / 3);

            for (CustomTrajectory customTrajectory : mCustomTrajectories)
            {
                double heightOfTriangle = preHeightOfTriangle;

                Polygon arrow;
                for (double time = 0; time < customTrajectory.getTrajectory().getTotalTimeSeconds(); time += dt)
                {
                    arrow = new Polygon(
                            0.0, -mSelectedRobot.getLengthPixels() / 4,
                            -mSelectedRobot.getWidthPixels() / 2, heightOfTriangle,
                            mSelectedRobot.getWidthPixels() / 2, heightOfTriangle
                    );

                    Translation2d sampledRobotPosition = customTrajectory.getTrajectory().sample(time).poseMeters.getTranslation();

                    arrow.setFill(Paint.valueOf(customTrajectory.getTrajectoryConfig().isReversed() ? "RED" : "GREEN"));
                    arrow.setStrokeType(StrokeType.INSIDE);
                    arrow.setStrokeWidth(2);
                    arrow.setStroke(Paint.valueOf("BLACK"));
                    arrow.setLayoutX(mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX());
                    arrow.setLayoutY(mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY());
                    arrow.setTranslateX(Constants.inchesToPixels(sampledRobotPosition.getX()));
                    arrow.setTranslateY(Constants.inchesToPixels(sampledRobotPosition.getY()));
                    arrow.setRotate(customTrajectory.getTrajectory().sample(time).poseMeters.getRotation().getDegrees() + 90);

                    if (customTrajectory.getTrajectoryConfig().isReversed())
                        arrow.setRotate(arrow.getRotate() + 180);

                    mProjectedPath.add(arrow);
                    mRoot.getChildren().add(arrow);
                }
            }

            updateArrowOwnership();
        }

        if (mSelectedTrajectory != null)
            for (Polygon arrow : mSelectedTrajectory.getArrows())
                arrow.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.valueOf("GREEN"), 10, 1, 0, 0));

        for (DraggableWaypoint waypoint : mWaypoints)
            waypoint.toFront();

        if (mTrajectoryFollowerState != TrajectoryFollowerState.STOPPED)
            mRobotRepresentation.toFront();
    }

    private TrajectoryConfig getTrajectoryConfig()
    {
        return new TrajectoryConfig(mSelectedRobot.getMaxVelocity(), mSelectedRobot.getMaxAcceleration())
                .setKinematics(mSelectedRobot.getDriveKinematics());
    }

    private double getAllTrajectoriesTotalTimeSeconds()
    {
        double totalTimeSeconds = 0;
        for (CustomTrajectory customTrajectory : mCustomTrajectories)
            totalTimeSeconds += customTrajectory.getTrajectory().getTotalTimeSeconds();
        return totalTimeSeconds;
    }

    private enum TrajectoryFollowerState
    {
        RUNNING, PAUSED, STOPPED
    }

    @Override
    public void start(Stage stage)
    {
        mStage = stage;

        stage.setOnCloseRequest(e ->
                System.exit(0));

        Scene scene = null;

        try
        {
            scene = new Scene(FXMLLoader.load(getClass().getResource("AutonDeveloper.fxml")));
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        stage.setTitle("Auton Developer");
        stage.setScene(scene);
        stage.show();
    }
}