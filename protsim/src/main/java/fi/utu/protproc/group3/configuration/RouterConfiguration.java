package fi.utu.protproc.group3.configuration;

import java.util.List;

public class RouterConfiguration extends NodeConfiguration {
    private List<InterfaceConfiguration> interfaces;
    private List<String> staticRoutes;
    private int autonomousSystem;
    private int inheritTrust;

    public List<InterfaceConfiguration> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceConfiguration> interfaces) {
        this.interfaces = interfaces;
    }

    public List<String> getStaticRoutes() {
        return staticRoutes;
    }

    public void setStaticRoutes(List<String> staticRoutes) {
        this.staticRoutes = staticRoutes;
    }

    public int getAutonomousSystem() {
        return autonomousSystem;
    }

    public void setAutonomousSystem(int autonomousSystem) {
        this.autonomousSystem = autonomousSystem;
    }

    public void setInheritTrust(int inheritTrust) {
        this.inheritTrust = inheritTrust;
    }

    public int getInheritTrust() {
        return inheritTrust;
    }
}
