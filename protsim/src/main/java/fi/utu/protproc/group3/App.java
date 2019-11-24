package fi.utu.protproc.group3;

import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        var exitCode = new CommandLine(new Simulator()).execute(args);
        System.exit(exitCode);
    }
}