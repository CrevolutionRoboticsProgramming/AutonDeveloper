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
    public Rectangle mRectangle;
    public ImageView mRotateIcon;
    public Text mRankText;
    private Point2D mMouseOffset;

    public DraggableWaypoint(double width, double length, double xOffset, double yOffset, double x, double y, int rank)
    {
        super();

        setLayoutX(xOffset);
        setLayoutY(yOffset);
        setTranslateX(x);
        setTranslateY(y);

        setWidth(width);
        setHeight(length);

        mRectangle = new Rectangle();

        mRectangle.setWidth(width);
        mRectangle.setHeight(length);

        mRectangle.setFill(Paint.valueOf("white"));

        mRectangle.setStroke(Paint.valueOf("black"));
        mRectangle.setStrokeType(StrokeType.INSIDE);
        mRectangle.setStrokeWidth(2);

        setOnMousePressed((event) -> mMouseOffset = new Point2D(event.getSceneX() - getDimensions().x, event.getSceneY() - getDimensions().y));

        mRankText = new Text();

        mRankText.setText(String.valueOf(rank));
        mRankText.setFont(Font.font("system", FontWeight.EXTRA_BOLD, 20));
        mRankText.setX(width / 2 - mRankText.getLayoutBounds().getWidth() / 2);
        mRankText.setY(length / 2 + mRankText.getLayoutBounds().getHeight() / 1.5);

        mRotateIcon = new ImageView(String.valueOf(getClass().getResource("/org/frc2851/RotateIcon.png")));
        mRotateIcon.setFitWidth(width / 2);
        mRotateIcon.setX((width - mRotateIcon.getFitWidth()) / 2);
        mRotateIcon.setFitHeight(mRotateIcon.getFitWidth());
        mRotateIcon.setY((length - mRotateIcon.getFitHeight()) / 6);

        getChildren().addAll(mRectangle, mRankText, mRotateIcon);
    }

    public void followMouse(MouseEvent mouseDraggedEvent)
    {
        setTranslateX(mouseDraggedEvent.getSceneX() - mMouseOffset.getX() + (getUnrotatedX() - getDimensions().x));
        setTranslateY(mouseDraggedEvent.getSceneY() - mMouseOffset.getY() + (getUnrotatedY() - getDimensions().y));
    }

    private void truncatePosition()
    {
        // Makes the x- and y-coordinates multiples of 0.25 for +1 prettiness
        setTranslateX(getTranslateX() - Util.inchesToPixels(getAdjustedXInches() % 0.25));
        setTranslateY(getTranslateY() + Util.inchesToPixels(getAdjustedYInches() % 0.25));
    }

    public void constrainTranslation(double x, double y, double width, double height)
    {
        if (getDimensions().x < x)
        {
            setTranslateX(x + (getUnrotatedX() - getDimensions().x));
        } else if (getDimensions().maxX > x + width)
        {
            setTranslateX(x + width - (getDimensions().width - (getUnrotatedX() - getDimensions().x)));
        }

        if (getDimensions().y < y)
        {
            setTranslateY(y + (getUnrotatedY() - getDimensions().y));
        } else if (getDimensions().maxY > y + height)
        {
            setTranslateY(y + height - (getDimensions().height - (getTranslateY() - getDimensions().y)));
        }

        truncatePosition();
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

        constrainTranslation(x, y, width, height);
    }

    public void setRank(int rank)
    {
        mRankText.setText(String.valueOf(rank));
    }

    public Dimensions getDimensions()
    {
        return new Dimensions();
    }

    public double getUnrotatedX()
    {
        return getTranslateX();
    }

    public double getUnrotatedY()
    {
        return getTranslateY();
    }

    public double getAdjustedXInches()
    {
        return Util.pixelsToInches(getDimensions().center.getX());
    }

    public double getAdjustedYInches()
    {
        // Position is measured with (0, 0) in the top-left corner; x increases rightwards, y increases downwards
        // This flips the measurement to make (0, 0) the bottom-left corner of the picture
        return 324 - Util.pixelsToInches(getDimensions().center.getY());
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
