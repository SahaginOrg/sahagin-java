package org.sahagin.share.runresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class RunResults implements YamlConvertible {
    private List<RootMethodRunResult> rootMethodRunResults = new ArrayList<>(512);

    public List<RootMethodRunResult> getRootMethodRunResults() {
        return rootMethodRunResults;
    }

    public void addRootMethodRunResults(RootMethodRunResult rootMethodRunResult) {
        this.rootMethodRunResults.add(rootMethodRunResult);
    }

    // returns null if not found
    public RootMethodRunResult getRunResultByRootMethodKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (RootMethodRunResult rootMethodRunResult : rootMethodRunResults) {
            if ((rootMethodRunResult.getRootMethod() != null)
                    && key.equals(rootMethodRunResult.getRootMethod().getKey())) {
                return rootMethodRunResult;
            }
        }
        return null;
    }

    // returns null if not found
    public RootMethodRunResult getRunResultByRootMethod(TestMethod rootMethod) {
        if (rootMethod == null) {
            throw new NullPointerException();
        }
        return getRunResultByRootMethodKey(rootMethod.getKey());
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<>(2);
        if (!rootMethodRunResults.isEmpty()) {
            result.put("rootMethodRunResults", YamlUtils.toYamlObjectList(rootMethodRunResults));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> rootMethodRunResultsYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "rootMethodRunResults", true);
        rootMethodRunResults = new ArrayList<>(rootMethodRunResultsYamlObj.size());
        for (Map<String, Object> rootMethodRunResultYamlObj : rootMethodRunResultsYamlObj) {
            RootMethodRunResult rootMethodRunResult = new RootMethodRunResult();
            rootMethodRunResult.fromYamlObject(rootMethodRunResultYamlObj);
            rootMethodRunResults.add(rootMethodRunResult);
        }
    }

    private void resolveTestMethod(SrcTree srcTree, StackLine stackLine)
            throws IllegalDataStructureException {
        if (!stackLine.getMethodKey().equals("")) {
            TestMethod testMethod = srcTree.getTestMethodByKey(stackLine.getMethodKey());
            stackLine.setMethod(testMethod);
        }
    }

    public void resolveKeyReference(SrcTree srcTree) throws IllegalDataStructureException {
        for (RootMethodRunResult runResult : rootMethodRunResults) {
            TestMethod rootMethod = srcTree.getTestMethodByKey(runResult.getRootMethodKey());
            runResult.setRootMethod(rootMethod);
            for (RunFailure failure : runResult.getRunFailures()) {
                for (StackLine stackLine : failure.getStackLines()) {
                    resolveTestMethod(srcTree, stackLine);
                }
            }
            for (LineScreenCapture capture : runResult.getLineScreenCaptures()) {
                for (StackLine stackLine : capture.getStackLines()) {
                    resolveTestMethod(srcTree, stackLine);
                }
            }
        }
    }


    // if the keys for this run results or the specified runResults have already been resolved,
    // you should call resolveKeyReference again for this run results after calling this method.
    // (since merging can break correct reference)
    public void merge(RunResults runResults) {
        for (RootMethodRunResult srcResult : runResults.rootMethodRunResults) {
            if (getRunResultByRootMethodKey(srcResult.getRootMethodKey()) != null) {
                throw new RuntimeException(String.format(
                        "not supported: root method %s is called multi times", srcResult.getRootMethodKey()));
            }
            addRootMethodRunResults(srcResult);
        }
    }
}
