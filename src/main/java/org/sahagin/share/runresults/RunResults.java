package org.sahagin.share.runresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class RunResults implements YamlConvertible {
    private List<RootFuncRunResult> rootFuncRunResults = new ArrayList<RootFuncRunResult>(512);

    public List<RootFuncRunResult> getRootFuncRunResults() {
        return rootFuncRunResults;
    }

    public void addRootFuncRunResults(RootFuncRunResult rootFuncRunResult) {
        this.rootFuncRunResults.add(rootFuncRunResult);
    }

    // returns null if not found
    public RootFuncRunResult getRunResultByRootFunction(TestFunction rootFunction) {
        if (rootFunction == null) {
            throw new NullPointerException();
        }
        for (RootFuncRunResult rootFuncRunResult : rootFuncRunResults) {
            if ((rootFuncRunResult.getRootFunction() != null)
                    && rootFunction.getKey().equals(rootFuncRunResult.getRootFunction().getKey())) {
                return rootFuncRunResult;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put("rootFuncRunResults", YamlUtils.toYamlObjectList(rootFuncRunResults));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> rootFuncRunResultsYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "rootFuncRunResults");
        rootFuncRunResults = new ArrayList<RootFuncRunResult>(rootFuncRunResultsYamlObj.size());
        for (Map<String, Object> rootFuncRunResultYamlObj : rootFuncRunResultsYamlObj) {
            RootFuncRunResult rootFuncRunResult = new RootFuncRunResult();
            rootFuncRunResult.fromYamlObject(rootFuncRunResultYamlObj);
            rootFuncRunResults.add(rootFuncRunResult);
        }
    }

    private void resolveTestFunction(SrcTree srcTree, StackLine stackLine)
            throws IllegalDataStructureException {
        if (!stackLine.getFunctionKey().equals("")) {
            TestFunction testFunction = srcTree.getTestFunctionByKey(stackLine.getFunctionKey());
            stackLine.setFunction(testFunction);
        }
    }

    public void resolveKeyReference(SrcTree srcTree) throws IllegalDataStructureException {
        for (RootFuncRunResult runResult : rootFuncRunResults) {
            TestFunction rootFunction = srcTree.getTestFunctionByKey(runResult.getRootFunctionKey());
            runResult.setRootFunction(rootFunction);
            for (RunFailure failure : runResult.getRunFailures()) {
                for (StackLine stackLine : failure.getStackLines()) {
                    resolveTestFunction(srcTree, stackLine);
                }
            }
            for (LineScreenCapture capture : runResult.getLineScreenCaptures()) {
                for (StackLine stackLine : capture.getStackLines()) {
                    resolveTestFunction(srcTree, stackLine);
                }
            }
        }
    }

}
