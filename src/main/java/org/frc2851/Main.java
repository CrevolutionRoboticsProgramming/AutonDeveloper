package org.frc2851;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.frc2851.field.Field;
import org.frc2851.field.Fields;
import org.frc2851.robot.Robot;
import org.frc2851.robot.Robots;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
    Button mCreateTrajectoryButton;
    @FXML
    Text mWaypointNumberText;
    @FXML
    Text mXText;
    @FXML
    Text mYText;
    @FXML
    Text mExitAngleText;
    @FXML
    ComboBox<String> mRobotsComboBox;
    @FXML
    ComboBox<String> mFieldsComboBox;
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
    Text mTimeText;
    @FXML
    Slider mTimeSlider;

    private Robot mSelectedRobot;

    private ArrayList<DraggableWaypoint> mWaypoints = new ArrayList<>();
    private DraggableWaypoint mSelectedWaypoint;

    private ArrayList<Rectangle> mProjectedPath = new ArrayList<>();

    private int mCurrentPlaybackTimeMs = 0;
    private int mDeltaTMs = 10;

    private Trajectory mTrajectoryToFollow;
    private Rectangle mRobotRectangle;
    private TrajectoryFollowerState mTrajectoryFollowerState = TrajectoryFollowerState.STOPPED;

    @FXML
    private void initialize()
    {
        for (Field field : Fields.fields)
        {
            mFieldsComboBox.getItems().add(field.getName());
        }
        mFieldsComboBox.getSelectionModel().selectFirst();

        for (Robot robot : Robots.robots)
        {
            mRobotsComboBox.getItems().add(robot.getName());
        }
        mRobotsComboBox.getSelectionModel().selectFirst();

        // Sets default to first field
        mFieldImageView.setImage(new Image(String.valueOf(getClass().getResource(Fields.fields[0].getPictureUrl()))));
        mFieldsComboBox.setOnAction(event ->
        {
            // Sets the image displayed in the field ImageView to the newly selected field
            mFieldImageView.setImage(new Image(Fields.fields[mFieldsComboBox.getSelectionModel().getSelectedIndex()].getPictureUrl()));
        });

        // Sets default to first robot
        mSelectedRobot = Robots.robots[0];
        mRobotRectangle = new Rectangle(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels());
        mRobotRectangle.setLayoutX(mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX());
        mRobotRectangle.setLayoutY(mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY());
        mRobotRectangle.setTranslateX(-1000);
        mRobotRectangle.setTranslateY(-1000);
        mRobotRectangle.setFill(Paint.valueOf("WHITE"));
        mRobotRectangle.setStrokeType(StrokeType.INSIDE);
        mRobotRectangle.setStrokeWidth(2);
        mRobotRectangle.setStroke(Paint.valueOf("BLACK"));
        mRoot.getChildren().add(mRobotRectangle);
        mRobotsComboBox.setOnAction(event ->
        {
            // Sets the selected robot to the newly selected robot
            mSelectedRobot = Robots.robots[mRobotsComboBox.getSelectionModel().getSelectedIndex()];

            mRobotRectangle.setWidth(mSelectedRobot.getWidthPixels());
            mRobotRectangle.setHeight(mSelectedRobot.getLengthPixels());
        });

        mFieldImageView.setOnMousePressed(event ->
        {
            mStopButton.fire();

            DraggableWaypoint newWaypoint = new DraggableWaypoint(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels(), mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX(),
                    mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY(), event.getX() - mSelectedRobot.getWidthPixels() / 2, event.getY() - mSelectedRobot.getLengthPixels() / 2,
                    mWaypoints.size() + 1);

            newWaypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());

            newWaypoint.mRectangle.setOnMouseDragged(mouseDraggedEvent ->
            {
                mStopButton.fire();
                newWaypoint.followMouse(mouseDraggedEvent);
                newWaypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                updateDisplay(newWaypoint);
            });

            newWaypoint.mRankText.setOnMouseDragged(newWaypoint.mRectangle.getOnMouseDragged());

            newWaypoint.mRotateIcon.setOnMouseDragged(mouseDraggedEvent ->
            {
                mStopButton.fire();
                newWaypoint.setRotate(90 - Math.toDegrees(Math.atan2((newWaypoint.getUnrotatedY() + newWaypoint.getHeight() / 2) - mouseDraggedEvent.getSceneY(),
                        mouseDraggedEvent.getSceneX() - (newWaypoint.getUnrotatedX() + newWaypoint.getWidth() / 2))));
                newWaypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                updateDisplay(newWaypoint);
            });

            mWaypoints.add(newWaypoint);
            mRoot.getChildren().add(newWaypoint);
        });

        mClearNodesButton.setOnMouseClicked(event ->
        {
            stopTrajectoryFollower();
            mRoot.getChildren().removeAll(mWaypoints);
            mWaypoints.clear();
        });

        mDeleteNodeButton.setOnMouseClicked(event ->
        {
            stopTrajectoryFollower();
            if (mSelectedWaypoint != null)
            {
                for (int i = mWaypoints.indexOf(mSelectedWaypoint) + 1; i < mWaypoints.size(); ++i)
                    mWaypoints.get(i).setRank(i);

                mRoot.getChildren().remove(mSelectedWaypoint);
                mWaypoints.remove(mSelectedWaypoint);

                updateDisplay(null);
            }
        });

        mCreateTrajectoryButton.setOnMouseClicked(event ->
        {
            double inchesToMeters = 0.0254;

            ArrayList<Pose2d> poses = new ArrayList<>();
            for (DraggableWaypoint waypoint : mWaypoints)
                poses.add(new Pose2d(waypoint.getAdjustedXInches(), waypoint.getAdjustedYInches(), Rotation2d.fromDegrees(-waypoint.getRotate() + 90)));

            /*
            StringBuilder trajectoryStringBuilder = new StringBuilder("Trajectory newTrajectory = TrajectoryGenerator.generateTrajectory(\n" +
                    "new Pose2d(" + (poses.get(0).getTranslation().getX() * inchesToMeters) + ", " + (poses.get(0).getTranslation().getY() * inchesToMeters) + ", new Rotation2d(Math.toRadians(" + Math.toDegrees(poses.get(0).getRotation().getRadians()) + "))),\n" +
                    "List.of(\n");

            for (int i = 1; i < poses.size() - 1; ++i)
            {
                trajectoryStringBuilder.append("new Translation2d(").append(poses.get(i).getTranslation().getX() * inchesToMeters).append(", ").append(poses.get(i).getTranslation().getY() * inchesToMeters).append(")");
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

            TrajectoryConfig config = new TrajectoryConfig(mSelectedRobot.getMaxVelocity(), mSelectedRobot.getMaxAcceleration()).setKinematics(mSelectedRobot.getDriveKinematics());
            Trajectory trajectory = TrajectoryGenerator.generateTrajectory(poses, config);

            try
            {
                double lastLeftVelocity = 0.0;
                double lastLeftAcceleration = 0.0;
                FileWriter leftTrajectoryFileWriter = new FileWriter("LeftTrajectory.csv");
                leftTrajectoryFileWriter.append("dt,x,y,position,velocity,acceleration,jerk,heading\n");
                double leftPosition = 0.0;
                for (int timeMs = 0; timeMs < trajectory.getTotalTimeSeconds() * 1000; ++timeMs)
                {
                    Trajectory.State state = trajectory.sample(timeMs / 1000.0);

                    double velocity = mSelectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.velocityMetersPerSecond * state.curvatureRadPerMeter)).leftMetersPerSecond;
                    double acceleration = velocity - lastLeftVelocity;
                    double jerk = acceleration - lastLeftAcceleration;

                    leftTrajectoryFileWriter.append(String.valueOf(1.0))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getX() - (state.poseMeters.getRotation().getSin() * mSelectedRobot.getWidthInches() / 2)))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY() + (state.poseMeters.getRotation().getCos() * mSelectedRobot.getLengthInches() / 2)))
                            .append(",").append(String.valueOf(leftPosition))
                            .append(",").append(String.valueOf(velocity))
                            .append(",").append(String.valueOf(acceleration))
                            .append(",").append(String.valueOf(jerk))
                            .append(",").append(String.valueOf(state.poseMeters.getRotation().getRadians()))
                            .append('\n');

                    leftPosition += velocity / 1000.0;

                    lastLeftVelocity = velocity;
                    lastLeftAcceleration = acceleration;
                }
                leftTrajectoryFileWriter.close();

                double lastRightVelocity = 0.0;
                double lastRightAcceleration = 0.0;
                FileWriter rightTrajectoryFileWriter = new FileWriter("RightTrajectory.csv");
                rightTrajectoryFileWriter.append("dt,x,y,position,velocity,acceleration,jerk,heading\n");
                double rightPosition = 0.0;
                for (int timeMs = 0; timeMs < trajectory.getTotalTimeSeconds() * 1000; ++timeMs)
                {
                    Trajectory.State state = trajectory.sample(timeMs / 1000.0);

                    double velocity = mSelectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.velocityMetersPerSecond * state.curvatureRadPerMeter)).rightMetersPerSecond;
                    double acceleration = velocity - lastRightVelocity;
                    double jerk = acceleration - lastRightAcceleration;

                    rightTrajectoryFileWriter.append(String.valueOf(1.0))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getX() + (state.poseMeters.getRotation().getSin() * mSelectedRobot.getWidthInches() / 2)))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY() - (state.poseMeters.getRotation().getCos() * mSelectedRobot.getLengthInches() / 2)))
                            .append(",").append(String.valueOf(rightPosition))
                            .append(",").append(String.valueOf(velocity))
                            .append(",").append(String.valueOf(acceleration))
                            .append(",").append(String.valueOf(jerk))
                            .append(",").append(String.valueOf(state.poseMeters.getRotation().getRadians()))
                            .append('\n');

                    rightPosition += velocity / 1000.0;

                    lastRightVelocity = velocity;
                    lastRightAcceleration = acceleration;
                }
                rightTrajectoryFileWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        mRoot.setOnMousePressed(mousePressedEvent ->
        {
            for (DraggableWaypoint waypoint : mWaypoints)
            {
                // If the mouse is inside the waypoint (waypoint.contains() didn't work for me)
                if (mousePressedEvent.getX() >= waypoint.getDimensions().x
                        && mousePressedEvent.getX() <= waypoint.getDimensions().x + waypoint.getWidth()
                        && mousePressedEvent.getY() >= waypoint.getDimensions().y
                        && mousePressedEvent.getY() <= waypoint.getDimensions().y + waypoint.getHeight())
                {
                    mStopButton.fire();

                    mSelectedWaypoint = waypoint;

                    updateDisplay(mSelectedWaypoint);

                    double smallOffset = Util.inchesToPixels(0.25);
                    double largeOffset = Util.inchesToPixels(1.0);

                    configureLongPressButton(mXSmallMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateX(waypoint.getTranslateX() - smallOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mXLargeMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateX(waypoint.getTranslateX() - largeOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mXSmallPlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateX(waypoint.getTranslateX() + smallOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mXLargePlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateX(waypoint.getTranslateX() + largeOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });

                    configureLongPressButton(mYSmallMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateY(waypoint.getTranslateY() + smallOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mYLargeMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateY(waypoint.getTranslateY() + largeOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mYSmallPlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateY(waypoint.getTranslateY() - smallOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mYLargePlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setTranslateY(waypoint.getTranslateY() - largeOffset);
                        waypoint.constrainTranslation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });

                    configureLongPressButton(mExitAngleSmallMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setRotate(waypoint.getRotate() - 0.25);
                        waypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mExitAngleLargeMinusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setRotate(waypoint.getRotate() - 1);
                        waypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mExitAngleSmallPlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setRotate(waypoint.getRotate() + 0.25);
                        waypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });
                    configureLongPressButton(mExitAngleLargePlusButton, () ->
                    {
                        mStopButton.fire();
                        waypoint.setRotate(waypoint.getRotate() + 1);
                        waypoint.constrainRotation(0, 0, mFieldImageView.getFitWidth(), mFieldImageView.getFitHeight());
                        updateDisplay(mSelectedWaypoint);
                    });

                    break;
                }
            }
        });

        final Timeline lineDrawer = new Timeline(
                new KeyFrame(Duration.ZERO, event ->
                {
                    for (Rectangle circle : mProjectedPath)
                        mRoot.getChildren().remove(circle);

                    mProjectedPath.clear();

                    if (mWaypoints.size() >= 2)
                    {
                        ArrayList<Pose2d> poses = new ArrayList<>();
                        for (DraggableWaypoint waypoint : mWaypoints)
                            poses.add(new Pose2d(Util.pixelsToInches(waypoint.getDimensions().center.getX()), Util.pixelsToInches(waypoint.getDimensions().center.getY()),
                                    Rotation2d.fromDegrees(waypoint.getRotate() - 90)));

                        TrajectoryConfig config = new TrajectoryConfig(mSelectedRobot.getMaxVelocity(), mSelectedRobot.getMaxAcceleration()).setKinematics(mSelectedRobot.getDriveKinematics());
                        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(poses, config);

                        for (double timeSeconds = 0; timeSeconds < trajectory.getTotalTimeSeconds(); timeSeconds += trajectory.getTotalTimeSeconds() / 30.0)
                        {
                            Rectangle rectangle = new Rectangle(mSelectedRobot.getWidthPixels(), mSelectedRobot.getLengthPixels());
                            rectangle.setFill(Paint.valueOf("BLACK"));
                            rectangle.setLayoutX(mFieldImageView.getLayoutX() + mFieldImageView.getTranslateX());
                            rectangle.setLayoutY(mFieldImageView.getLayoutY() + mFieldImageView.getTranslateY());
                            rectangle.setTranslateX(Util.inchesToPixels(trajectory.sample(timeSeconds).poseMeters.getTranslation().getX()) - mSelectedRobot.getWidthPixels() / 2);
                            rectangle.setTranslateY(Util.inchesToPixels(trajectory.sample(timeSeconds).poseMeters.getTranslation().getY()) - mSelectedRobot.getLengthPixels() / 2);
                            rectangle.setRotate(trajectory.sample(timeSeconds).poseMeters.getRotation().getDegrees() + 90);

                            if (rectangle.getTranslateX() > 0
                                    && rectangle.getTranslateX() + rectangle.getWidth() < mFieldImageView.getTranslateX() + mFieldImageView.getFitWidth()
                                    && rectangle.getTranslateY() > mFieldImageView.getTranslateY()
                                    && rectangle.getTranslateY() + rectangle.getHeight() < mFieldImageView.getTranslateY() + mFieldImageView.getFitHeight())
                            {
                                mProjectedPath.add(rectangle);
                                mRoot.getChildren().add(rectangle);
                            }
                        }
                    }

                    for (DraggableWaypoint waypoint : mWaypoints)
                        waypoint.toFront();

                    if (mTrajectoryFollowerState != TrajectoryFollowerState.STOPPED)
                        mRobotRectangle.toFront();
                }),
                new KeyFrame(Duration.millis(25))
        );
        lineDrawer.setCycleCount(Timeline.INDEFINITE);

        lineDrawer.play();

        final Timeline trajectoryFollower = new Timeline(
                new KeyFrame(Duration.ZERO, event ->
                {
                    if (mTrajectoryFollowerState == TrajectoryFollowerState.RUNNING)
                    {
                        if (mCurrentPlaybackTimeMs < mTrajectoryToFollow.getTotalTimeSeconds() * 1000)
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
                    ArrayList<Pose2d> poses = new ArrayList<>();
                    for (DraggableWaypoint waypoint : mWaypoints)
                        poses.add(new Pose2d(Util.pixelsToInches(waypoint.getDimensions().center.getX()), Util.pixelsToInches(waypoint.getDimensions().center.getY()),
                                Rotation2d.fromDegrees(waypoint.getRotate() - 90)));

                    TrajectoryConfig config = new TrajectoryConfig(mSelectedRobot.getMaxVelocity(), mSelectedRobot.getMaxAcceleration()).setKinematics(mSelectedRobot.getDriveKinematics());
                    mTrajectoryToFollow = TrajectoryGenerator.generateTrajectory(poses, config);

                    startTrajectoryFollower();
                    break;
                case RUNNING:
                    mStartPauseButton.setText("Start");
                    mTrajectoryFollowerState = TrajectoryFollowerState.PAUSED;
                    break;
                case PAUSED:
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
    }

    private void updateDisplay(DraggableWaypoint waypoint)
    {
        if (waypoint == null)
        {
            mWaypointNumberText.setText("");
            mXText.setText("0.0");
            mYText.setText("0.0");
            mExitAngleText.setText("0.0");
        } else
        {
            DecimalFormat formatter = new DecimalFormat("000.00");
            mWaypointNumberText.setText(String.valueOf(mWaypoints.indexOf(waypoint) + 1));
            mXText.setText(formatter.format(waypoint.getAdjustedXInches()));
            mYText.setText(formatter.format(waypoint.getAdjustedYInches()));
            mExitAngleText.setText(formatter.format(waypoint.getRotate()));
        }
    }

    private void startTrajectoryFollower()
    {
        mTimeSlider.setMax(mTrajectoryToFollow.getTotalTimeSeconds());
        mStartPauseButton.setText("Pause");
        mTrajectoryFollowerState = TrajectoryFollowerState.RUNNING;
    }

    private void stopTrajectoryFollower()
    {
        mCurrentPlaybackTimeMs = 0;
        mTimeSlider.setMax(1);
        mStartPauseButton.setText("Start");
        mRobotRectangle.toBack();
        updateTrajectoryFollowerDisplay();
        mTrajectoryFollowerState = TrajectoryFollowerState.STOPPED;
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
                {
                    Event.fireEvent(button, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 0,
                            true, true, true, true, true, true, true, true, true, true, null));
                }
            }
        };

        button.addEventFilter(MouseEvent.ANY, event ->
        {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
                timer.start();
            if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
                timer.stop();
        });

        button.setOnMouseClicked(event ->
                onClick.run());
    }

    private void updateTrajectoryFollowerDisplay()
    {
        DecimalFormat formatter = new DecimalFormat("00.000");
        mTimeText.setText(formatter.format(mCurrentPlaybackTimeMs / 1000.0));
        mTimeSlider.setValue(mCurrentPlaybackTimeMs / 1000.0);
    }

    private void advanceTrajectory()
    {
        Trajectory.State state = mTrajectoryToFollow.sample(mCurrentPlaybackTimeMs / 1000.0);
        mRobotRectangle.setTranslateX(Util.inchesToPixels(state.poseMeters.getTranslation().getX()) - mSelectedRobot.getWidthPixels() / 2);
        mRobotRectangle.setTranslateY(Util.inchesToPixels(state.poseMeters.getTranslation().getY()) - mSelectedRobot.getLengthPixels() / 2);
        mRobotRectangle.setRotate(state.poseMeters.getRotation().getDegrees() + 90);
        mRobotRectangle.toFront();
    }

    private enum TrajectoryFollowerState
    {
        RUNNING, PAUSED, STOPPED
    }

    @Override
    public void start(Stage stage)
    {
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