package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.AddressGenerator;

public interface SimulationBuilderContext {
    AddressGenerator generator();
    Network network(String name);

    Simulation simulation();
}
