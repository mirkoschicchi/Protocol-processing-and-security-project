package fi.utu.protproc.group3;

import fi.utu.protproc.group3.simulator.Simulation;
import picocli.CommandLine;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        var exitCode = new CommandLine(new Simulator()).execute(args);
        System.exit(exitCode);
    }
}
