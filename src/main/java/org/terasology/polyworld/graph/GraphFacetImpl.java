// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.polyworld.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFacet2D;
import org.terasology.math.geom.Vector2i;
import org.terasology.polyworld.TriangleLookup;
import org.terasology.polyworld.rp.WorldRegion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides a collection of {@link Graph}s that cover the entire facet.
 */
public class GraphFacetImpl extends BaseFacet2D implements GraphFacet {

    private final Map<WorldRegion, Graph> graphs = Maps.newLinkedHashMap();
    private final List<TriangleLookup> lookups = Lists.newArrayList();

    public GraphFacetImpl(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    /**
     * @param graph the graph to add (must overlap the facet area)
     */
    public void add(WorldRegion wr, Graph graph, TriangleLookup lookup) {
        Preconditions.checkArgument(wr.getArea().equals(graph.getBounds()), "region does not match graph");
        Preconditions.checkArgument(graph.getBounds().equals(lookup.getBounds()), "graph does not match triangle " +
                "lookup");

        graphs.put(wr, graph);
        lookups.add(lookup);
    }

    @Override
    public Graph getWorld(int x, int z) {
        for (Graph g : graphs.values()) {
            if (g.getBounds().contains(x, z)) {
                return g;
            }
        }

        throw new IllegalArgumentException(String.format("no graph data for %d/%d", x, z));
    }

    @Override
    public Graph getWorld(Vector2i pos) {
        return getWorld(pos.x, pos.y);
    }

    @Override
    public Triangle getWorldTriangle(int x, int z) {
        for (TriangleLookup lookup : lookups) {
            if (lookup.getBounds().contains(x, z)) {
                return lookup.findTriangleAt(x, z);
            }
        }

        throw new IllegalArgumentException(String.format("no triangle lookup data for %d/%d", x, z));
    }

    @Override
    public Graph get(int x, int z) {
        int wx = x - getRelativeRegion().minX() + getWorldRegion().minX();
        int wz = z - getRelativeRegion().minY() + getWorldRegion().minY();
        return getWorld(wx, wz);
    }

    @Override
    public Graph get(Vector2i pos) {
        return get(pos.x, pos.y);
    }

    @Override
    public Graph getGraph(WorldRegion wr) {
        return graphs.get(wr);
    }

    @Override
    public Collection<Graph> getAllGraphs() {
        return Collections.unmodifiableCollection(graphs.values());
    }
}
