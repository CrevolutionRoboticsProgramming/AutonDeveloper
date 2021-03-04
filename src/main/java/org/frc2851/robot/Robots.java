package org.frc2851.robot;

public class Robots
{
    public static Robot hyperion = new Robot("Hyperion", 28, 48, 24, 180, 120, 720);
    public static Robot asteria = new Robot("Asteria", 32, 38, 24, 94, 71, 720); // 94 in/sec max, 71 in/sec/sec max, ? in/sec/sec/sec max
    public static Robot aegis = new Robot("Aegis", 33.5, 37.5, 23.5, 94, 126, 6000);

    public static Robot[] robots = {aegis, asteria, hyperion};
}
