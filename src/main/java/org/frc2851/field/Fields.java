package org.frc2851.field;

public class Fields
{
    // The field itself is 52 feet x 26 feet
    // The ImageView in which the field is displayed is 936 x 468; each pixel represents 2/3 inches when a full-sized field is displayed
    public static Field deepSpace = new Field("Deep Space", "/org/frc2851/field/DeepSpace.png", 52*12, 26*12);
    public static Field infiniteRecharge = new Field("Infinite Recharge", "/org/frc2851/field/InfiniteRecharge.png", 52*12, 26*12);
    public static Field infiniteRechargeGalacticSearchA = new Field("Galactic Search A", "/org/frc2851/field/GalacticSearchA.png", 30*12, 15*12);
    public static Field infiniteRechargeGalacticSearchB = new Field("Galactic Search B", "/org/frc2851/field/GalacticSearchB.png", 30*12, 15*12);
    public static Field infiniteRechargeBarrel = new Field("Barrel", "/org/frc2851/field/Barrel.png", 30*12, 15*12);
    public static Field infiniteRechargeSlalom = new Field("Slalom", "/org/frc2851/field/Slalom.png", 30*12, 15*12);
    public static Field infiniteRechargeBounce = new Field("Bounce", "/org/frc2851/field/Bounce.png", 30*12, 15*12);
    public static Field rapidReact = new Field("Rapid React", "/org/frc2851/field/RapidReact.png", 54*12, 27*12);

    public static Field[] fields = {infiniteRechargeGalacticSearchA, infiniteRechargeGalacticSearchB,
            infiniteRechargeBarrel, infiniteRechargeSlalom, infiniteRechargeBounce, infiniteRecharge,
            deepSpace, rapidReact};
}
