package lambda;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;
import base.Java8TestBase;


public class TestMain extends Java8TestBase {

    @Test
    public void streamApiCall() {
        List<String> slist = new ArrayList<String>(2);
        slist.add("a");
        slist.add("b");
        slist.stream().forEach(s -> print(s));
    }

    @TestDoc("Doc: print")
    public void print(String str) {
        if (str.equals("c")) {
            System.out.println(str);
        }
    }
}
