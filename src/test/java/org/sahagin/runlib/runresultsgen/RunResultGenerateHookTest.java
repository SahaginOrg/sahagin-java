package org.sahagin.runlib.runresultsgen;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.sahagin.TestBase;

public class RunResultGenerateHookTest extends TestBase {

    // TODO calling another maven process make it hard to analyze this test result..
    //@Test
    public void successResult() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        //request.setPomFile(new File("pom.xml"));
        request.setGoals(Arrays.asList("jar:jar"));
        // avoid recursive test call
        //request.setMavenOpts("-Dmaven.test.skip=true");

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);
        File jar = new File("target/sahagin-0.2.2-SNAPSHOT.jar");
        assertTrue(jar.exists());
        assertThat(result.getExitCode(), is(0));
        //System.err.println(invoker.getWorkingDirectory());
        //System.out.println(invoker.getWorkingDirectory());




    }
}
