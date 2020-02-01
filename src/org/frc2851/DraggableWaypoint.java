package org.frc2851;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class DraggableWaypoint extends Region
{
    public Rectangle rectangle;
    public ImageView rotateIcon;
    public Text rankText;
    private Point2D mouseOffset;

    public DraggableWaypoint(double width, double length, double x, double y, int rank)
    {
        super();

        setLayoutX(0);
        setLayoutY(0);
        setTranslateX(x);
        setTranslateY(y);

        setWidth(width);
        setHeight(length);

        rectangle = new Rectangle();

        rectangle.setWidth(width);
        rectangle.setHeight(length);

        rectangle.setFill(Paint.valueOf("white"));

        rectangle.setStroke(Paint.valueOf("black"));
        rectangle.setStrokeType(StrokeType.INSIDE);
        rectangle.setStrokeWidth(2);

        setOnMousePressed(event ->
                mouseOffset = new Point2D(event.getSceneX() - getDimensions().x, event.getSceneY() - getDimensions().y));

        rankText = new Text();

        rankText.setText(String.valueOf(rank));
        rankText.setFont(Font.font("system", FontWeight.EXTRA_BOLD, 20));
        rankText.setX(width / 2 - rankText.getLayoutBounds().getWidth() / 2);
        rankText.setY(length / 2 + rankText.getLayoutBounds().getHeight() / 1.5);

        rotateIcon = new ImageView(String.valueOf(getClass().getResource("resources/rotateIcon.png")));
        rotateIcon.setFitWidth(width / 2);
        rotateIcon.setX((width - rotateIcon.getFitWidth()) / 2);
        rotateIcon.setFitHeight(rotateIcon.getFitWidth());
        rotateIcon.setY((length - rotateIcon.getFitHeight()) / 6);

        getChildren().add(rectangle);
        getChildren().add(rankText);
        getChildren().add(rotateIcon);
    }

    public void followMouse(MouseEvent mouseDraggedEvent)
    {
        setTranslateX(mouseDraggedEvent.getSceneX() - mouseOffset.getX() + (getUnrotatedX() - getDimensions().x));
        setTranslateY(mouseDraggedEvent.getSceneY() - mouseOffset.getY() + (getUnrotatedY() - getDimensions().y));
    }

    private void truncatePosition(double xOffset, double yOffset, boolean right, boolean up)
    {
        // Makes the x- and y-coordinates multiples of 0.25 for +1 prettiness
        setTranslateX(Math.round(getTranslateX() * 4.0) / 4.0);
        setTranslateY(Math.round(getTranslateY() * 4.0) / 4.0);

        for (int i = 0; i < 2 && getAdjustedXInches(xOffset) % 0.25 != 0; ++i)
        {
            setTranslateX(right ? getTranslateX() + 0.25 : getTranslateX() - 0.25);
        }
        for (int i = 0; i < 2 && getAdjustedYInches(yOffset) % 0.25 != 0; ++i)
        {
            setTranslateY(up ? getTranslateY() - 0.25 : getTranslateY() + 0.25);
        }
    }

    public void constrainPosition(double x, double y, double width, double height)
    {
        boolean truncateRight = true, truncateUp = true;

        if (getDimensions().x < x)
        {
            setTranslateX(x + (getUnrotatedX() - getDimensions().x));
        } else if (getDimensions().maxX > x + width)
        {
            setTranslateX(x + width - (getDimensions().width - (getUnrotatedX() - getDimensions().x)));
            truncateRight = false;
        }

        if (getDimensions().y < y)
        {
            setTranslateY(y + (getUnrotatedY() - getDimensions().y));
            truncateUp = false;
        } else if (getDimensions().maxY > y + height)
        {
            setTranslateY(y + height - (getDimensions().height - (getTranslateY() - getDimensions().y)));
        }

        truncatePosition(x, y, truncateRight, truncateUp);
    }

    public void constrainRotation(double x, double y, double width, double height)
    {
        // Makes the angle a multiple of 0.25 for +1 prettiness
        setRotate(Math.round(getRotate() * 4.0) / 4.0);

        if (getRotate() > 180)
        {
            setRotate(getRotate() - 360);
        } else if (getRotate() < -180)
        {
            setRotate(getRotate() + 360);
        }

        constrainPosition(x, y, width, height);
    }

    public void setRank(int rank)
    {
        rankText.setText(String.valueOf(rank));
    }

    public Dimensions getDimensions()
    {
        return new Dimensions();
    }

    public double getUnrotatedX()
    {
        return getLayoutX() + getTranslateX();
    }

    public double getUnrotatedY()
    {
        return getLayoutY() + getTranslateY();
    }

    public double getAdjustedXInches(double offset)
    {
        return Util.scaleDimensionDown(getUnrotatedX() + getWidth() / 2 - offset);
    }

    public double getAdjustedYInches(double offset)
    {
        // Position is measured with (0, 0) in the top-left corner; x increases rightwards, y increases downwards
        // This flips the measurement to make (0, 0) the bottom-left corner of the picture
        return 324 - Util.scaleDimensionDown(getUnrotatedY() + getHeight() / 2 - offset);
    }

    private Point2D getRotatedPoint(Point2D center, Point2D point, double rotation)
    {
        double angleAtZeroDegrees = Math.toDegrees(Math.atan2((point.getY() - center.getY()), (point.getX() - center.getX())));
        double hypotenuse = center.distance(point);

        return new Point2D(center.getX() + hypotenuse * Math.cos(Math.toRadians(angleAtZeroDegrees + rotation)), center.getY() + hypotenuse * Math.sin(Math.toRadians(angleAtZeroDegrees + rotation)));
    }

    public class Dimensions
    {
        public Point2D[] corners;
        public double x, y;
        public double minX, minY, maxX, maxY;
        public double width, height;
        public Point2D center;

        public Dimensions()
        {
            minX = Double.POSITIVE_INFINITY;
            minY = Double.POSITIVE_INFINITY;
            maxX = Double.NEGATIVE_INFINITY;
            maxY = Double.NEGATIVE_INFINITY;

            corners = new Point2D[]
                    {
                            new Point2D(getUnrotatedX(), getUnrotatedY()),
                            new Point2D(getUnrotatedX() + getWidth(), getUnrotatedY()),
                            new Point2D(getUnrotatedX(), getUnrotatedY() + getHeight()),
                            new Point2D(getUnrotatedX() + getWidth(), getUnrotatedY() + getHeight())
                    };

            center = new Point2D(getUnrotatedX() + getWidth() / 2, getUnrotatedY() + getHeight() / 2);

            for (Point2D point : corners)
            {
                if (getRotatedPoint(center, point, getRotate()).getX() > maxX)
                    maxX = getRotatedPoint(center, point, getRotate()).getX();
                if (getRotatedPoint(center, point, getRotate()).getX() < minX)
                    minX = getRotatedPoint(center, point, getRotate()).getX();
            }

            for (Point2D point : corners)
            {
                if (getRotatedPoint(center, point, getRotate()).getY() > maxY)
                    maxY = getRotatedPoint(center, point, getRotate()).getY();
                if (getRotatedPoint(center, point, getRotate()).getY() < minY)
                    minY = getRotatedPoint(center, point, getRotate()).getY();
            }

            x = minX;
            y = minY;

            width = maxX - minX;
            height = maxY - minY;
        }
    }
}
