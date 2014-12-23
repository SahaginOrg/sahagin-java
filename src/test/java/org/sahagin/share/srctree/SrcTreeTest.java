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
        File expected = new File(testResourceDir("yamlConversion"), "srcTree");
        File actual = new File(mkWorkDir("yamlConversion"), "srcTree");

        Map<String, Object> fromYamlObj = YamlUtils.load(expected);
        SrcTree srcTree = new SrcTree();
        srcTree.fromYamlObject(fromYamlObj);
        Map<String, Object> toYamlObj = srcTree.toYamlObject();
        YamlUtils.dump(toYamlObj, actual);

        TestBase.assertFileContentsEquals(expected, actual);

    }


}
