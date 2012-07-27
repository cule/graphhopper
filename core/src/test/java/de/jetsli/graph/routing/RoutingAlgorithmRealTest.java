/*
 *  Copyright 2012 Peter Karich info@jetsli.de
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetsli.graph.routing;

import de.jetsli.graph.reader.OSMReader;
import de.jetsli.graph.reader.RoutingAlgorithmIntegrationTests;
import de.jetsli.graph.storage.Graph;
import de.jetsli.graph.storage.Location2IDIndex;
import de.jetsli.graph.storage.Location2IDQuadtree;
import de.jetsli.graph.util.CmdArgs;
import de.jetsli.graph.util.Helper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich
 */
public class RoutingAlgorithmRealTest {

    RoutingAlgorithmIntegrationTests.TestAlgoCollector testCollector;

    @Before
    public void setUp() {
        testCollector = new RoutingAlgorithmIntegrationTests.TestAlgoCollector();
    }

    @Test
    public void testMonaco() {
        List<OneRun> list = new ArrayList<OneRun>();
        list.add(new OneRun(43.727687, 7.418737, 43.730729, 7.421288, 1.532, 88));
        list.add(new OneRun(43.727687, 7.418737, 43.74958, 7.436566, 3.448, 136));
        runAlgo(testCollector, "files/monaco.osm.gz", "target/graph-monaco", list);
        
        assertEquals(testCollector.toString(), 0, testCollector.list.size());
    }

    @Test
    public void testAndorra() {
        List<OneRun> list = new ArrayList<OneRun>();
        list = new ArrayList<OneRun>();
        list.add(new OneRun(42.56819, 1.603231, 42.571034, 1.520662, 16.378, 636));
        list.add(new OneRun(42.529176, 1.571302, 42.571034, 1.520662, 12.429, 397));
        runAlgo(testCollector, "files/andorra.osm.gz", "target/graph-andorra", list);
        
        assertEquals(testCollector.toString(), 0, testCollector.list.size());
    }

    void runAlgo(RoutingAlgorithmIntegrationTests.TestAlgoCollector testCollector, String osmFile,
            String graphFile, List<OneRun> forEveryAlgo) {
        try {
            // make sure we are using the latest file format
            Helper.deleteDir(new File(graphFile));
            Graph g = OSMReader.osm2Graph(new CmdArgs().put("osm", osmFile).put("graph", graphFile));
            // System.out.println(osmFile + " - all locations " + g.getNodes());
            Location2IDIndex idx = new Location2IDQuadtree(g).prepareIndex(2000);
            RoutingAlgorithm[] algos = RoutingAlgorithmIntegrationTests.createAlgos(g);
            for (RoutingAlgorithm algo : algos) {
                int failed = testCollector.list.size();

                for (OneRun or : forEveryAlgo) {
                    int from = idx.findID(or.fromLat, or.fromLon);
                    int to = idx.findID(or.toLat, or.toLon);
                    testCollector.assertDistance(algo, from, to, or.dist, or.locs);
                }

//                System.out.println(osmFile + " " + algo.getClass().getSimpleName()
//                        + ": " + (testCollector.list.size() - failed) + " failed");
            }
        } catch (Exception ex) {
            throw new RuntimeException("cannot handle osm file " + osmFile, ex);
        } finally {
            Helper.deleteDir(new File(graphFile));
        }
    }

    class OneRun {

        double fromLat, fromLon;
        double toLat, toLon;
        double dist;
        int locs;

        public OneRun(double fromLat, double fromLon, double toLat, double toLon, double dist, int locs) {
            this.fromLat = fromLat;
            this.fromLon = fromLon;
            this.toLat = toLat;
            this.toLon = toLon;
            this.dist = dist;
            this.locs = locs;
        }
    }
}