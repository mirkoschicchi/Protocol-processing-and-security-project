package fi.utu.protproc.group3;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "run", description = "Runs a simulation until stopped by the user")
public class Simulator implements Callable<Integer> {
    @CommandLine.Parameters(description = "The scenario file to load.")
    private File scenarioFile;

    @CommandLine.Option(names = { "-w", "--write" }, description = "Write network traffic to file (ngpcap file format)")
    private File networkFile;

    @Override
    public Integer call() throws Exception {
        SimulationConfiguration config = null;

        var fis = new FileInputStream(scenarioFile);
        try {
            config = SimulationConfiguration.parse(fis);
        } finally {
            fis.close();
        }

        var sim = Simulation.create(config);
        if (networkFile != null) {
            System.out.println("Writing pcap dump to " + networkFile.getAbsolutePath());
            sim.start(networkFile.getAbsolutePath());
        } else {
            sim.start(null);
        }

        sim.show();

        System.out.println("Simulation running. Press enter to stop simulation.");
        System.in.read();

        sim.stop();
        System.out.println("Simulation stopped.");

        return 0;
    }
}
