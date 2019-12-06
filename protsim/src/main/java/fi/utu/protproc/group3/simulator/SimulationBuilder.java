package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;

interface SimulationBuilder {
    Simulation load(SimulationConfiguration configuration);
}
