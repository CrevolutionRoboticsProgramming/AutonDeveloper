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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
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
    Pane root;
    @FXML
    ImageView fieldImageView;
    @FXML
    Button clearNodesButton;
    @FXML
    Button deleteNodeButton;
    @FXML
    Button xSmallMinusButton;
    @FXML
    Button xLargeMinusButton;
    @FXML
    Button xSmallPlusButton;
    @FXML
    Button xLargePlusButton;
    @FXML
    Button ySmallMinusButton;
    @FXML
    Button yLargeMinusButton;
    @FXML
    Button ySmallPlusButton;
    @FXML
    Button yLargePlusButton;
    @FXML
    Button exitAngleSmallMinusButton;
    @FXML
    Button exitAngleLargeMinusButton;
    @FXML
    Button exitAngleSmallPlusButton;
    @FXML
    Button exitAngleLargePlusButton;
    @FXML
    Button createTrajectoryButton;
    @FXML
    Text waypointNumberText;
    @FXML
    Text xText;
    @FXML
    Text yText;
    @FXML
    Text exitAngleText;
    @FXML
    ComboBox<String> robotsComboBox;
    @FXML
    ComboBox<String> fieldsComboBox;

    private Robot selectedRobot;

    private ArrayList<DraggableWaypoint> waypoints = new ArrayList<>();
    private DraggableWaypoint selectedWaypoint;

    private ArrayList<Rectangle> projectedPath = new ArrayList<>();

    @FXML
    private void initialize()
    {
        for (Field field : Fields.fields)
        {
            fieldsComboBox.getItems().add(field.getName());
        }
        fieldsComboBox.getSelectionModel().selectFirst();

        for (Robot robot : Robots.robots)
        {
            robotsComboBox.getItems().add(robot.getName());
        }
        robotsComboBox.getSelectionModel().selectFirst();

        // Sets default to first field
        fieldImageView.setImage(new Image(String.valueOf(getClass().getResource(Fields.fields[0].getPictureUrl()))));
        fieldsComboBox.setOnAction(event ->
        {
            // Sets the image displayed in the field ImageView to the newly selected field
            fieldImageView.setImage(new Image(Fields.fields[fieldsComboBox.getSelectionModel().getSelectedIndex()].getPictureUrl()));
        });

        // Sets default to first robot
        selectedRobot = Robots.robots[0];
        robotsComboBox.setOnAction(event ->
        {
            // Sets the selected robot to the newly selected robot
            selectedRobot = Robots.robots[robotsComboBox.getSelectionModel().getSelectedIndex()];
        });

        fieldImageView.setOnMousePressed(event ->
        {
            DraggableWaypoint newWaypoint = new DraggableWaypoint(selectedRobot.getWidthPixels(), selectedRobot.getLengthPixels(), event.getX(), event.getY(), waypoints.size() + 1);

            newWaypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                    fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());

            newWaypoint.rectangle.setOnMouseDragged(mouseDraggedEvent ->
            {
                newWaypoint.followMouse(mouseDraggedEvent);
                newWaypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                        fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                updateDisplay(newWaypoint);
            });

            newWaypoint.rankText.setOnMouseDragged(newWaypoint.rectangle.getOnMouseDragged());

            newWaypoint.rotateIcon.setOnMouseDragged(mouseDraggedEvent ->
            {
                newWaypoint.setRotate(90 - Math.toDegrees(Math.atan2((newWaypoint.getUnrotatedY() + newWaypoint.getHeight() / 2) - mouseDraggedEvent.getSceneY(),
                        mouseDraggedEvent.getSceneX() - (newWaypoint.getUnrotatedX() + newWaypoint.getWidth() / 2))));
                newWaypoint.constrainRotation(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                        fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                updateDisplay(newWaypoint);
            });

            waypoints.add(newWaypoint);
            root.getChildren().add(newWaypoint);
        });

        clearNodesButton.setOnMouseClicked(event ->
        {
            root.getChildren().removeAll(waypoints);
            waypoints.clear();
        });

        deleteNodeButton.setOnMouseClicked(event ->
        {
            if (selectedWaypoint != null)
            {
                for (int i = waypoints.indexOf(selectedWaypoint) + 1; i < waypoints.size(); ++i)
                    waypoints.get(i).setRank(i);

                root.getChildren().remove(selectedWaypoint);
                waypoints.remove(selectedWaypoint);

                updateDisplay(null);
            }
        });

        createTrajectoryButton.setOnMouseClicked(event ->
        {
            double inchesToMeters = 0.0254;

            ArrayList<Pose2d> poses = new ArrayList<>();
            for (int i = 0; i < waypoints.size(); ++i)
            {
                poses.add(new Pose2d(waypoints.get(i).getAdjustedXInches(fieldImageView.getLayoutX() + fieldImageView.getTranslateX()),
                        waypoints.get(i).getAdjustedYInches(fieldImageView.getLayoutY() + fieldImageView.getTranslateY()),
                        Rotation2d.fromDegrees(waypoints.get(i).getRotate() - 90)));
            }

            /*
            StringBuilder trajectoryStringBuilder = new StringBuilder("Trajectory newTrajectory = TrajectoryGenerator.generateTrajectory(\n" +
                    "new Pose2d(" + (points[0].x * inchesToMeters) + ", " + (points[0].y * inchesToMeters) + ", new Rotation2d(Math.toRadians(" + Math.toDegrees(points[0].angle) + "))),\n" +
                    "List.of(\n");

            for (int i = 1; i < points.length - 1; ++i)
            {
                trajectoryStringBuilder.append("new Translation2d(").append(points[i].x * inchesToMeters).append(", ").append(points[i].y * inchesToMeters).append(")");
                if (i != points.length - 2)
                    trajectoryStringBuilder.append(",");
                trajectoryStringBuilder.append("\n");
            }

            trajectoryStringBuilder.append("),\n")
                    .append("new Pose2d(")
                    .append(points[points.length - 1].x * inchesToMeters)
                    .append(", ").append(points[points.length - 1].y * inchesToMeters)
                    .append(", new Rotation2d(Math.toRadians(")
                    .append(Math.toDegrees(points[points.length - 1].angle))
                    .append("))),\n")
                    .append("config\n")
                    .append(");");

            System.out.println(trajectoryStringBuilder);
            */

            TrajectoryConfig config = new TrajectoryConfig(selectedRobot.getMaxVelocity(), selectedRobot.getMaxAcceleration());
            Trajectory trajectory = TrajectoryGenerator.generateTrajectory(poses, config);

            try
            {
                FileWriter leftTrajectoryFileWriter = new FileWriter("LeftTrajectory.csv");
                double leftPosition = 0.0;
                for (int time = 0; time < trajectory.getTotalTimeSeconds() * 1000; ++time)
                {
                    Trajectory.State state = trajectory.sample(time / 1000.0);
                    leftTrajectoryFileWriter.append(String.valueOf(0.001)).append(",").append(String.valueOf(state.poseMeters.getTranslation().getX()))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY())).append(",").append(String.valueOf(leftPosition))
                            .append(",").append(String.valueOf(selectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.curvatureRadPerMeter)).leftMetersPerSecond))
                            .append('\n');

                    leftPosition += selectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.curvatureRadPerMeter)).leftMetersPerSecond * time;
                }
                leftTrajectoryFileWriter.close();

                FileWriter rightTrajectoryFileWriter = new FileWriter("RightTrajectory.csv");
                double rightPosition = 0.0;
                for (int time = 0; time < trajectory.getTotalTimeSeconds() * 1000; ++time)
                {
                    Trajectory.State state = trajectory.sample(time / 1000.0);
                    rightTrajectoryFileWriter.append(String.valueOf(0.001)).append(",").append(String.valueOf(state.poseMeters.getTranslation().getX()))
                            .append(",").append(String.valueOf(state.poseMeters.getTranslation().getY())).append(",").append(String.valueOf(rightPosition))
                            .append(",").append(String.valueOf(selectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.curvatureRadPerMeter)).rightMetersPerSecond))
                            .append('\n');

                    rightPosition += selectedRobot.getDriveKinematics().toWheelSpeeds(
                            new ChassisSpeeds(state.velocityMetersPerSecond, 0, state.curvatureRadPerMeter)).rightMetersPerSecond * time;
                }
                rightTrajectoryFileWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        root.setOnMousePressed(mousePressedEvent ->
        {
            for (DraggableWaypoint waypoint : waypoints)
            {
                // If the mouse is inside the waypoint (waypoint.contains() didn't work for me)
                if (mousePressedEvent.getX() >= waypoint.getDimensions().x
                        && mousePressedEvent.getX() <= waypoint.getDimensions().x + waypoint.getWidth()
                        && mousePressedEvent.getY() >= waypoint.getDimensions().y
                        && mousePressedEvent.getY() <= waypoint.getDimensions().y + waypoint.getHeight())
                {
                    selectedWaypoint = waypoint;

                    updateDisplay(selectedWaypoint);

                    double smallOffset = Util.scaleDimensionUp(0.25);
                    double largeOffset = Util.scaleDimensionUp(1.0);

                    configureLongPressButton(xSmallMinusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() - smallOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xLargeMinusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() - largeOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xSmallPlusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() + smallOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xLargePlusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() + largeOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });

                    configureLongPressButton(ySmallMinusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() + smallOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(yLargeMinusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() + largeOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(ySmallPlusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() - smallOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(yLargePlusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() - largeOffset);
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });

                    configureLongPressButton(exitAngleSmallMinusButton, () ->
                    {
                        waypoint.setRotate(waypoint.getRotate() - 0.25);
                        waypoint.constrainRotation(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(exitAngleLargeMinusButton, () ->
                    {
                        waypoint.setRotate(waypoint.getRotate() - 1);
                        waypoint.constrainRotation(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(exitAngleSmallPlusButton, () ->
                    {
                        waypoint.setRotate(waypoint.getRotate() + 0.25);
                        waypoint.constrainRotation(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(exitAngleLargePlusButton, () ->
                    {
                        waypoint.setRotate(waypoint.getRotate() + 1);
                        waypoint.constrainRotation(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });

                    break;
                }
            }
        });

        final Timeline lineDrawer = new Timeline(
                new KeyFrame(Duration.ZERO, event ->
                {
                    for (Rectangle circle : projectedPath)
                    {
                        root.getChildren().remove(circle);
                    }

                    projectedPath.clear();

                    if (waypoints.size() >= 2)
                    {
                        ArrayList<Pose2d> poses = new ArrayList<>();
                        for (int i = 0; i < waypoints.size(); ++i)
                        {
                            poses.add(new Pose2d(waypoints.get(i).getAdjustedXInches(fieldImageView.getLayoutX() + fieldImageView.getTranslateX()),
                                    waypoints.get(i).getAdjustedYInches(fieldImageView.getLayoutY() + fieldImageView.getTranslateY()),
                                    Rotation2d.fromDegrees(waypoints.get(i).getRotate() - 90)));
                        }

                        TrajectoryConfig config = new TrajectoryConfig(selectedRobot.getMaxVelocity(), selectedRobot.getMaxAcceleration());
                        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(poses, config);

                        for (double time = 0; time < trajectory.getTotalTimeSeconds(); time += trajectory.getTotalTimeSeconds() / 30.0)
                        {
                            Rectangle rectangle = new Rectangle();
                            rectangle.setWidth(selectedRobot.getWidthPixels());
                            rectangle.setHeight(selectedRobot.getLengthPixels());
                            rectangle.setFill(Paint.valueOf("BLACK"));
                            rectangle.setX(Util.scaleDimensionUp(trajectory.sample(time).poseMeters.getTranslation().getX()) - 10);
                            rectangle.setY(Util.scaleDimensionUp(trajectory.sample(time).poseMeters.getTranslation().getY()) - 14.5);
                            rectangle.setRotate(trajectory.sample(time).poseMeters.getRotation().getDegrees() + 90);

                            if (rectangle.getX() > fieldImageView.getLayoutX() + fieldImageView.getTranslateX()
                                    && rectangle.getX() + rectangle.getWidth() < fieldImageView.getLayoutX() + fieldImageView.getTranslateX() + fieldImageView.getFitWidth()
                                    && rectangle.getY() > fieldImageView.getLayoutY() + fieldImageView.getTranslateY()
                                    && rectangle.getY() + rectangle.getHeight() < fieldImageView.getLayoutY() + fieldImageView.getTranslateY() + fieldImageView.getFitHeight())
                            {
                                projectedPath.add(rectangle);
                                root.getChildren().add(rectangle);
                            }
                        }

                        for (DraggableWaypoint waypoint : waypoints)
                        {
                            waypoint.toFront();
                        }
                    }
                }),
                new KeyFrame(Duration.millis(25))
        );
        lineDrawer.setCycleCount(Timeline.INDEFINITE);

        lineDrawer.play();
    }

    private void updateDisplay(DraggableWaypoint waypoint)
    {
        if (waypoint == null)
        {
            waypointNumberText.setText("");
            xText.setText("0.0");
            yText.setText("0.0");
            exitAngleText.setText("0.0");
        } else
        {
            DecimalFormat formatter = new DecimalFormat("000.00");
            waypointNumberText.setText(String.valueOf(waypoints.indexOf(waypoint) + 1));
            xText.setText(formatter.format(waypoint.getAdjustedXInches(fieldImageView.getLayoutX() + fieldImageView.getTranslateX())));
            yText.setText(formatter.format(waypoint.getAdjustedYInches(fieldImageView.getLayoutY() + fieldImageView.getTranslateY())));
            exitAngleText.setText(formatter.format(waypoint.getRotate()));
        }
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