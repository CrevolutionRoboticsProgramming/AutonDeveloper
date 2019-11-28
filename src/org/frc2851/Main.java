package org.frc2851;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.modifiers.TankModifier;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;

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

    private Vector<DraggableWaypoint> waypoints = new Vector<>();
    private DraggableWaypoint selectedWaypoint;

    private Vector<Circle> projectedPath = new Vector<>();

    @FXML
    private void initialize()
    {
        for (Field field : Fields.fields)
        {
            fieldsComboBox.getItems().add(field.name);
        }
        fieldsComboBox.getSelectionModel().selectFirst();

        for (Robot robot : Robots.robots)
        {
            robotsComboBox.getItems().add(robot.name);
        }
        robotsComboBox.getSelectionModel().selectFirst();

        // Sets default to first field
        fieldImageView.setImage(new Image(String.valueOf(getClass().getResource(Fields.fields[0].pictureUrl))));
        fieldsComboBox.setOnAction(event ->
        {
            // Sets the image displayed in the field ImageView to the newly selected field
            fieldImageView.setImage(new Image(Fields.fields[fieldsComboBox.getSelectionModel().getSelectedIndex()].pictureUrl));
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
            DraggableWaypoint newWaypoint = new DraggableWaypoint(selectedRobot.width, selectedRobot.length, event.getX(), event.getY(), waypoints.size() + 1);

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
            Waypoint[] points = new Waypoint[waypoints.size()];
            for (int i = 0; i < waypoints.size(); ++i)
            {
                points[i] = new Waypoint(waypoints.elementAt(i).getUnrotatedX() + waypoints.elementAt(i).getWidth() / 2,
                        waypoints.elementAt(i).getAdjustedY(fieldImageView.getLayoutY() + fieldImageView.getTranslateY()),
                        Pathfinder.d2r(waypoints.elementAt(i).getRotate() + 90));
            }

            Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
                    0.05, selectedRobot.maxVelocity, selectedRobot.maxAcceleration, selectedRobot.maxJerk);
            Trajectory trajectory = Pathfinder.generate(points, config);

            TankModifier modifier = new TankModifier(trajectory);
            modifier.modify(selectedRobot.wheelbase);

            Trajectory left = modifier.getLeftTrajectory();
            Trajectory right = modifier.getRightTrajectory();

            File leftFile = new File("LeftTrajectory.csv");
            Pathfinder.writeToCSV(leftFile, left);
            File rightFile = new File("RightTrajectory.csv");
            Pathfinder.writeToCSV(rightFile, right);
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

                    configureLongPressButton(xSmallMinusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() - (3.0 / 8.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xLargeMinusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() - (3.0 / 2.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xSmallPlusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() + (3.0 / 8.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(xLargePlusButton, () ->
                    {
                        waypoint.setTranslateX(waypoint.getTranslateX() + (3.0 / 2.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });

                    configureLongPressButton(ySmallMinusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() + (3.0 / 8.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(yLargeMinusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() + (3.0 / 2.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(ySmallPlusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() - (3.0 / 8.0));
                        waypoint.constrainPosition(fieldImageView.getLayoutX() + fieldImageView.getTranslateX(),
                                fieldImageView.getLayoutY() + fieldImageView.getTranslateY(), fieldImageView.getFitWidth(), fieldImageView.getFitHeight());
                        updateDisplay(selectedWaypoint);
                    });
                    configureLongPressButton(yLargePlusButton, () ->
                    {
                        waypoint.setTranslateY(waypoint.getTranslateY() - (3.0 / 2.0));
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
                    if (waypoints.size() >= 2)
                    {
                        for (Circle circle : projectedPath)
                        {
                            root.getChildren().remove(circle);
                        }

                        projectedPath.clear();

                        Waypoint[] points = new Waypoint[waypoints.size()];
                        for (int i = 0; i < waypoints.size(); ++i)
                        {
                            points[i] = new Waypoint(waypoints.elementAt(i).getUnrotatedX() + waypoints.elementAt(i).getWidth() / 2,
                                    waypoints.elementAt(i).getUnrotatedY() + waypoints.elementAt(i).getHeight() / 2,
                                    Pathfinder.d2r(waypoints.elementAt(i).getRotate() + 90));
                        }

                        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, 1,
                                1, selectedRobot.maxVelocity, selectedRobot.maxAcceleration, selectedRobot.maxJerk);
                        Trajectory trajectory = Pathfinder.generate(points, config);

                        for (int i = 0; i < trajectory.segments.length; ++i)
                        {
                            int radius = 5;

                            Circle circle = new Circle();
                            circle.setRadius(radius);
                            circle.setFill(Paint.valueOf("white"));
                            circle.setStroke(Paint.valueOf("black"));
                            circle.setStrokeType(StrokeType.INSIDE);
                            circle.setStrokeWidth(2);
                            circle.setCenterX(trajectory.segments[i].x);
                            circle.setCenterY(trajectory.segments[i].y);

                            boolean intersect = false;
                            for (DraggableWaypoint waypoint : waypoints)
                            {
                                if (waypoint.getBoundsInParent().intersects(circle.getBoundsInParent()))
                                    intersect = true;
                            }

                            if (!intersect
                                    && circle.getCenterX() - circle.getRadius() > fieldImageView.getLayoutX() + fieldImageView.getTranslateX()
                                    && circle.getCenterX() + circle.getRadius() < fieldImageView.getLayoutX() + fieldImageView.getTranslateX() + fieldImageView.getFitWidth()
                                    && circle.getCenterY() - circle.getRadius() > fieldImageView.getLayoutY() + fieldImageView.getTranslateY()
                                    && circle.getCenterY() + circle.getRadius() < fieldImageView.getLayoutY() + fieldImageView.getTranslateY() + fieldImageView.getFitHeight())
                            {
                                projectedPath.add(circle);
                                root.getChildren().add(circle);
                            }
                        }
                    } else
                    {
                        for (Circle circle : projectedPath)
                        {
                            root.getChildren().remove(circle);
                        }

                        projectedPath.clear();
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
            xText.setText(formatter.format(waypoint.getAdjustedX(fieldImageView.getLayoutX() + fieldImageView.getTranslateX())));
            yText.setText(formatter.format(waypoint.getAdjustedY(fieldImageView.getLayoutY() + fieldImageView.getTranslateY())));
            exitAngleText.setText(formatter.format(waypoint.getRotate()));
        }
    }

    private void configureLongPressButton(Button button, OnClick onClick)
    {
        final AnimationTimer timer = new AnimationTimer()
        {
            private long baseline = 0;

            @Override
            public void start()
            {
                baseline = System.currentTimeMillis();
                super.start();
            }

            @Override
            public void handle(long time)
            {
                if (System.currentTimeMillis() - baseline > 500
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

    interface OnClick
    {
        void run();
    }
}