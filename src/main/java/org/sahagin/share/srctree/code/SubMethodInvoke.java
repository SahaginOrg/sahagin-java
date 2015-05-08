package org.sahagin.share.srctree.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;

public class SubMethodInvoke extends Code {
    public static final String TYPE = "method";

    private String subMethodKey;
    private TestMethod subMethod;
    private List<Code> args = new ArrayList<Code>(4);
    private Code thisInstance;
    // whether the actual invoked method is the child method of the method for the subMethodKey
    private boolean childInvoke = false;

    public String getSubMethodKey() {
        return subMethodKey;
    }

    public void setSubMethodKey(String subMethodKey) {
        this.subMethodKey = subMethodKey;
    }

    public TestMethod getSubMethod() {
        return subMethod;
    }

    public void setSubMethod(TestMethod subMethod) {
        this.subMethod = subMethod;
    }

    public List<Code> getArgs() {
        return args;
    }

    public void addArg(Code arg) {
        args.add(arg);
    }

    public Code getThisInstance() {
        return thisInstance;
    }

    public void setThisInstance(Code thisInstance) {
        this.thisInstance = thisInstance;
    }

    public boolean isChildInvoke() {
        return childInvoke;
    }

    public void setChildInvoke(boolean childInvoke) {
        this.childInvoke = childInvoke;
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("methodKey", subMethodKey);
        if (!args.isEmpty()) {
            result.put("args", YamlUtils.toYamlObjectList(args));
        }
        if (thisInstance != null) {
            result.put("thisInstance", YamlUtils.toYamlObject(thisInstance));
        }
        if (childInvoke) {
            result.put("childInvoke", childInvoke);
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        subMethodKey = YamlUtils.getStrValue(yamlObject, "methodKey");
        subMethod = null;
        List<Map<String, Object>> argsYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "args", true);
        args = new ArrayList<Code>(argsYamlObj.size());
        for (Map<String, Object> argYamlObj : argsYamlObj) {
            Code code = Code.newInstanceFromYamlObject(argYamlObj);
            args.add(code);
        }
        Map<String, Object> thisInstanceYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "thisInstance", true);
        thisInstance = Code.newInstanceFromYamlObject(thisInstanceYamlObj);
        Boolean childInvokeObj = YamlUtils.getBooleanValue(yamlObject, "childInvoke", true);
        if (childInvokeObj == null) {
            childInvoke = false;
        } else {
            childInvoke = childInvokeObj;
        }
    }

}
