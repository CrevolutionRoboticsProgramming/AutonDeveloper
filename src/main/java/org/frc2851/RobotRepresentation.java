package org.frc2851;

import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;

public class RobotRepresentation extends Region
{
    private Rectangle mRectangle;
    private Polygon mTriangle;

    public RobotRepresentation(double width, double height)
    {
        mRectangle = new Rectangle(width, height);
        mRectangle.setFill(Paint.valueOf("WHITE"));
        mRectangle.setStrokeType(StrokeType.INSIDE);
        mRectangle.setStrokeWidth(2);
        mRectangle.setStroke(Paint.valueOf("BLACK"));
        getChildren().add(mRectangle);

        mTriangle = new Polygon();
        adjustTriangleDimensions();
        getChildren().add(mTriangle);
    }

    public void setWidthHeight(double width, double height)
    {
        mRectangle.setWidth(width);
        mRectangle.setHeight(height);
        adjustTriangleDimensions();
    }

    private void adjustTriangleDimensions()
    {
        mTriangle.getPoints().setAll(
                0.0, 0.0,
                -mRectangle.getWidth()/4, mRectangle.getHeight()/5,
                mRectangle.getWidth()/4, mRectangle.getHeight()/5);
        mTriangle.setLayoutX(mRectangle.getWidth()/2);
        mTriangle.setLayoutY(10);
    }
}
