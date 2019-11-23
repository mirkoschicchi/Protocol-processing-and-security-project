package fi.utu.protproc.group3.configuration;

import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NetworkConfiguration {
    private String name;
    private String address;
    private int autonomousSystem;

    private List<NodeConfiguration> clients;
    private int randomClients;
    private List<NodeConfiguration> servers;
    private int randomServers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<NodeConfiguration> getClients() {
        return clients;
    }

    public void setClients(List<NodeConfiguration> clients) {
        this.clients = clients;
    }

    public int getRandomClients() {
        return randomClients;
    }

    public void setRandomClients(int randomClients) {
        this.randomClients = randomClients;
    }

    public List<NodeConfiguration> getServers() {
        return servers;
    }

    public void setServers(List<NodeConfiguration> servers) {
        this.servers = servers;
    }

    public int getRandomServers() {
        return randomServers;
    }

    public void setRandomServers(int randomServers) {
        this.randomServers = randomServers;
    }

    public Stream<NodeConfiguration> getActualServers() {
        Stream<NodeConfiguration> stream = servers != null ? servers.stream() : Stream.empty();
        return Stream.concat(stream, IntStream.range(0, getRandomServers()).mapToObj(i -> new NodeConfiguration()));
    }

    public Stream<NodeConfiguration> getActualClients() {
        Stream<NodeConfiguration> stream = clients != null ? clients.stream() : Stream.empty();
        return Stream.concat(stream, IntStream.range(0, getRandomClients()).mapToObj(i -> new NodeConfiguration()));
    }

    public int getAutonomousSystem() {
        return autonomousSystem;
    }

    public void setAutonomousSystem(int autonomousSystem) {
        this.autonomousSystem = autonomousSystem;
    }
}
