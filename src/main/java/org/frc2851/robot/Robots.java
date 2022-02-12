package org.frc2851.robot;

public class Robots
{
    public static Robot hyperion = new Robot("Hyperion", 28, 48, 24, 180, 120);
    public static Robot asteria = new Robot("Asteria", 32, 38, 24, 94, 71);
    public static Robot aegis = new Robot("Aegis", 33.5, 38, 27.5, 80, 20);//94, 126);

    public static Robot[] robots = {aegis, asteria, hyperion};
}
