package fi.utu.protproc.group3;

import fi.utu.protproc.group3.simulator.Simulation;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        var sim = Simulation.create();
        var ispLink = sim.createNetwork();

        var localNet1 = sim.createNetwork();
        sim.createClient(localNet1);
        sim.createRouter(localNet1, ispLink);

        var localNet2 = sim.createNetwork();
        sim.createServer(localNet2);
        sim.createServer(localNet2);
        sim.createServer(localNet2);
        sim.createRouter(localNet2, ispLink);

        sim.start("/tmp/ProtSimTest.pcap");

        System.out.println("Simulation running for 10s...");

        Thread.sleep(10000);

        sim.stop();
        System.out.println("Simulation stopped.");
    }
}
