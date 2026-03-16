package com.showtime.robot.app;

import com.showtime.robot.app.cli.RobotCli;
import picocli.CommandLine;

public final class RobotMain {
  public static void main(String[] args) {
    int exitCode = new CommandLine(new RobotCli()).execute(args);
    System.exit(exitCode);
  }
}
