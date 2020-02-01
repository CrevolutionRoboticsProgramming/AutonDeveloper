package org.frc2851.field;

public class Field
{
    private String mName;
    private String mPictureUrl;

    public Field(String name, String pictureUrl)
    {
        this.mName = name;
        this.mPictureUrl = pictureUrl;
    }

    public String getName()
    {
        return mName;
    }

    public String getPictureUrl()
    {
        return mPictureUrl;
    }
}
