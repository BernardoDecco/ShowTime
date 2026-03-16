package com.horadoshow.robot.app;

import com.horadoshow.robot.app.cli.RobotCli;
import picocli.CommandLine;

public final class RobotMain {
  public static void main(String[] args) {
    int exitCode = new CommandLine(new RobotCli()).execute(args);
    System.exit(exitCode);
  }
}

