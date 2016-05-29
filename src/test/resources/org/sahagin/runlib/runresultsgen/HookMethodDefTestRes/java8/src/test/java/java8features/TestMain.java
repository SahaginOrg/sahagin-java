package java8features;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

import base.Java8TestBase;


public class TestMain extends Java8TestBase {

    @Test
    public void streamApiCallTest() {
        print("a");
        List<String> slist = new ArrayList<String>(2);
        slist.add("b");
        slist.stream().forEach(s -> System.out.println(s));
        slist.stream().forEach(s -> print(s));
    }

    @TestDoc("Doc: print")
    public void print(String str) {}
    
    @Test
    public void defaultInterfaceTest() {
        InterfaceWithDefault instance = new InterfaceWithDefault() {};
        instance.defaultMethod();
    }

    public interface InterfaceWithDefault {

        @TestDoc("Doc: defaultMethod")
        default void defaultMethod() {}
    }
}
