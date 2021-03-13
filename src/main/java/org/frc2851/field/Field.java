package org.frc2851.field;

public class Field
{
    private String mName;
    private String mPictureUrl;
    private double mLength;
    private double mWidth;

    public Field(String name, String pictureUrl, double length, double width)
    {
        mName = name;
        mPictureUrl = pictureUrl;
        mLength = length;
        mWidth = width;
    }

    public String getName()
    {
        return mName;
    }

    public String getPictureUrl()
    {
        return mPictureUrl;
    }

    // The width of the field ImageView is hard-coded. I'm sorry.
    public double getPixelsToInchesRatio()
    {
        return 936.0/mLength;
    }

    public double getLength()
    {
        return mLength;
    }

    public double getWidth()
    {
        return mWidth;
    }
}
