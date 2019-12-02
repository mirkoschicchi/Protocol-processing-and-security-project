package fi.utu.protproc.group3;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.simulator.Simulation;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "run", description = "Runs a simulation until stopped by the user")
public class Simulator implements Callable<Integer> {
    @CommandLine.Parameters(description = "The scenario file to load.")
    private File scenarioFile;

    @CommandLine.Option(names = { "-w", "--write" }, description = "Write network traffic to file (ngpcap file format)")
    private File networkFile;

    @CommandLine.Option(names = { "-n", "--network" }, description = "Limit traffic capture to the given network")
    private String network;

    @CommandLine.Option(names = { "-q", "--no-gui" }, description = "Disables the simulation UI")
    private boolean noGui;

    @Override
    public Integer call() throws Exception {
        SimulationConfiguration config;
        var fis = new FileInputStream(scenarioFile);
        try {
            config = SimulationConfiguration.parse(fis);
        } finally {
            fis.close();
        }

        var sim = Simulation.create(config);
        if (networkFile != null) {
            System.out.println("Writing pcap dump to " + networkFile.getAbsolutePath());
            sim.start(networkFile.getAbsolutePath(), network);
        } else {
            sim.start();
        }

        if (!noGui) {
            sim.show();
        }

        System.out.println("Simulation running. Press enter to stop simulation.");
        System.in.read();

        System.out.println("Shutting down nodes...");
        sim.stop();
        System.out.println("Simulation stopped.");

        return 0;
    }
}
