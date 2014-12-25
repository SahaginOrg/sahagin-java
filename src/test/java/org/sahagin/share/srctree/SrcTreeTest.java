package org.sahagin.share.srctree;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class SrcTreeTest extends TestBase {

    // convert YAML -> srcTree -> YAML, then compare 2 YAML
    @Test
    public void yamlConversion() throws YamlConvertException {
        File fromYamlFile = new File(testResourceDir("yamlConversion"), "srcTree");
        Map<String, Object> fromYamlObj = YamlUtils.load(fromYamlFile);
        SrcTree srcTree = new SrcTree();
        srcTree.fromYamlObject(fromYamlObj);
        Map<String, Object> toYamlObj = srcTree.toYamlObject();
        assertYamlEquals(fromYamlObj, toYamlObj);
    }

}
