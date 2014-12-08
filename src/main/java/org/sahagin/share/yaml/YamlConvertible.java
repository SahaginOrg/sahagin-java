package org.sahagin.share.yaml;

import java.util.Map;

public interface YamlConvertible {

    public Map<String, Object> toYamlObject();

    // classKeys and functionKeys are set by this method,
    // but TestClass and TestFunction references are not set
    public void fromYamlObject(Map<String, Object> yamlObject) throws YamlConvertException;

}
