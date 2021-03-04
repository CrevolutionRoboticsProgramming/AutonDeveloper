package org.frc2851.field;

public class Field
{
    private String mName;
    private String mPictureUrl;
    private double mPixelsToInchesRatio;

    public Field(String name, String pictureUrl, double pixelsToInchesRatio)
    {
        this.mName = name;
        this.mPictureUrl = pictureUrl;
        this.mPixelsToInchesRatio = pixelsToInchesRatio;
    }

    public String getName()
    {
        return mName;
    }

    public String getPictureUrl()
    {
        return mPictureUrl;
    }

    public double getPixelsToInchesRatio()
    {
        return mPixelsToInchesRatio;
    }
}
