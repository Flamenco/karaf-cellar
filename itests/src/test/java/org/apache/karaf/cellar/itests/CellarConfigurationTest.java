/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.karaf.cellar.itests;

import org.apache.karaf.cellar.core.ClusterManager;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static org.junit.Assert.*;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class CellarConfigurationTest extends CellarTestSupport {

    private static final String TESTPID = "org.apache.karaf.cellar.tst";

    @Test
    @Ignore
    public void testCellarFeaturesModule() throws InterruptedException {
        installCellar();
        createCellarChild("child1");
        createCellarChild("child2");
        Thread.sleep(DEFAULT_TIMEOUT);
        ClusterManager clusterManager = getOsgiService(ClusterManager.class);
        assertNotNull(clusterManager);

        String node1 = getNodeIdOfChild("child1");
        String node2 = getNodeIdOfChild("child2");
        System.err.println(executeCommand("admin:list"));

        String properties = executeCommand("admin:connect child1 config:proplist --pid " + TESTPID);
        System.err.println(properties);
        assertFalse((properties.contains("myKey")));

        //Test configuration sync - add property
        System.err.println(executeCommand("config:propset --pid " + TESTPID + " myKey myValue"));
        Thread.sleep(5000);
        properties = executeCommand("admin:connect child1 config:proplist --pid " + TESTPID);
        System.err.println(properties);
        assertTrue(properties.contains("myKey = myValue"));

        //Test configuration sync - remove property
        System.err.println(executeCommand("config:propdel --pid " + TESTPID + " myKey"));
        Thread.sleep(5000);
        properties = executeCommand("admin:connect child1 config:proplist --pid " + TESTPID);
        System.err.println(properties);
        assertFalse(properties.contains("myKey"));


        //Test configuration sync - add property - join later
        System.err.println(executeCommand("cluster:group-set new-grp " + node1));
        Thread.sleep(5000);
        System.err.println(executeCommand("admin:connect child1 config:propset --pid " + TESTPID + " myKey2 myValue2"));
        properties = executeCommand("admin:connect child1 config:proplist --pid " + TESTPID);
        Thread.sleep(5000);
        System.err.println(executeCommand("cluster:group-set new-grp " + node2));
        properties = executeCommand("admin:connect child2 config:proplist --pid " + TESTPID);
        System.err.println(properties);
        assertTrue(properties.contains("myKey2 = myValue2"));
    }

    @After
    public void tearDown() {
        try {
            destroyCellarChild("child1");
            destroyCellarChild("child2");
            unInstallCellar();
        } catch (Exception ex) {
            //Ignore
        }
    }

}
