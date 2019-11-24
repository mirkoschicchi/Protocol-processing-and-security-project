package fi.utu.protproc.group3.configuration;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class SimulationConfiguration {
    public static SimulationConfiguration parse(InputStream stream) {
        Objects.requireNonNull(stream);

        var parser = new Yaml();

        return parser.loadAs(stream, SimulationConfiguration.class);
    }


    private String name;
    private String description;

    private List<NetworkConfiguration> networks;
    private List<RouterConfiguration> routers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<NetworkConfiguration> getNetworks() {
        return networks;
    }

    public void setNetworks(List<NetworkConfiguration> networks) {
        this.networks = networks;
    }

    public List<RouterConfiguration> getRouters() {
        return routers;
    }

    public void setRouters(List<RouterConfiguration> routers) {
        this.routers = routers;
    }
}
