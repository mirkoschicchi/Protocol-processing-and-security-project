package fi.utu.protproc.group3.utils;

import fi.utu.protproc.group3.nodes.RouterNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ASPath {
    public static final ASPath LOCAL = new ASPath();

    private final List<Short> autonomousSystems, bgpIdentifiers;
    private final List<List<Short>> additionalLists = new ArrayList<>();

    private ASPath() {
        autonomousSystems = new ArrayList<>();
        bgpIdentifiers = new ArrayList<>();
    }

    public ASPath(List<List<Short>> networkFormat) {
        Objects.requireNonNull(networkFormat);

        autonomousSystems = new ArrayList<>(networkFormat.get(0));
        bgpIdentifiers = new ArrayList<>(networkFormat.get(1));

        for (var i = 2; i < networkFormat.size(); i++) {
            additionalLists.add(networkFormat.get(i));
        }
    }

    public ASPath(RouterNode router) {
        this();

        Objects.requireNonNull(router);

        autonomousSystems.add((short) router.getAutonomousSystem());
        bgpIdentifiers.add((short) router.getBGPIdentifier());
    }

    public int length() {
        return autonomousSystems.size();
    }

    public boolean containsRouter(RouterNode router) {
        return bgpIdentifiers.contains((short) router.getBGPIdentifier());
    }

    public ASPath add(RouterNode router) {
        var result = new ASPath();

        result.autonomousSystems.add((short) router.getAutonomousSystem());
        result.autonomousSystems.addAll(this.autonomousSystems);
        result.bgpIdentifiers.add((short) router.getBGPIdentifier());
        result.bgpIdentifiers.addAll(this.bgpIdentifiers);

        return result;
    }

    public List<List<Short>> toNetworkFormat() {
        List<List<Short>> result = new ArrayList<>();

        result.add(Collections.unmodifiableList(autonomousSystems));
        result.add(Collections.unmodifiableList(bgpIdentifiers));
        result.addAll(additionalLists);

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autonomousSystems, bgpIdentifiers);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASPath) {
            var other = (ASPath) obj;
            return Objects.equals(autonomousSystems, other.autonomousSystems)
                    && Objects.equals(bgpIdentifiers, other.bgpIdentifiers);
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        for (var i = 0; i < autonomousSystems.size(); i++) {
            if (result.length() > 0) result.append(',');
            result.append("AS").append(autonomousSystems.get(i)).append('@').append(bgpIdentifiers.get(i));
        }

        return result.toString();
    }
}
