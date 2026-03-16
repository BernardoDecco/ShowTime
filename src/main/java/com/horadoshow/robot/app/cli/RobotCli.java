package com.horadoshow.robot.app.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
    name = "investment-robot",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "Swing-trade robot starter (market data + analysis + broker abstraction).",
    subcommands = {
        CandlesCommand.class,
        AnalyzeCommand.class
    }
)
public final class RobotCli implements Runnable {
  @Spec
  CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(), "Missing command. Try: candles, analyze");
  }
}

