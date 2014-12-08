package org.sahagin.share.srctree.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;

public class SubFunctionInvoke extends Code {
    public static final String TYPE = "function";

    private String subFunctionKey;
    private TestFunction subFunction;
    private List<Code> args = new ArrayList<Code>(4);

    public String getSubFunctionKey() {
        return subFunctionKey;
    }

    public void setSubFunctionKey(String subFunctionKey) {
        this.subFunctionKey = subFunctionKey;
    }

    public TestFunction getSubFunction() {
        return subFunction;
    }

    public void setSubFunction(TestFunction subFunction) {
        this.subFunction = subFunction;
    }

    public List<Code> getArgs() {
        return args;
    }

    public void addArg(Code arg) {
        args.add(arg);
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    protected String getFunctionKeyName() {
        return "functionKey";
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put(getFunctionKeyName(), subFunctionKey);
        result.put("args", YamlUtils.toYamlObjectList(args));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        subFunctionKey = YamlUtils.getStrValue(yamlObject, getFunctionKeyName());
        subFunction = null;
        List<Map<String, Object>> argsYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "args");
        args = new ArrayList<Code>(argsYamlObj.size());
        for (Map<String, Object> argYamlObj : argsYamlObj) {
            Code code = Code.newInstanceFromYamlObject(argYamlObj);
            args.add(code);
        }
    }

}
