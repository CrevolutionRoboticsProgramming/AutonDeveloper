package org.frc2851.robot;

public class Robots
{
    public static Robot hyperion = new Robot("Hyperion", 28, 48, 24, 180, 120, 720);
    public static Robot asteria = new Robot("Asteria", 32, 38, 24, 30, 30, 120); // 94 in/sec max, 71 in/sec/sec max, ? in/sec/sec/sec max

    public static Robot[] robots = {asteria, hyperion};
}
