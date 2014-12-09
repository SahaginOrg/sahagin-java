// global slider object
var slider = null;
// global srcTree object;
var srcTree = null;

// slideIndex..integer
function getSlideTtId(slideIndex) {
  return $(".bxslider div.scrollContainer").eq(slideIndex).attr("data-tt-id");
};

// trObject..jQuery object for table tr element
function getTrTtId(trObject) {
  return trObject.attr("data-tt-id");
};

// -1 means not found
function getTtIdSlideIndex(ttId) {
  var slideObj = $(".bxslider div.scrollContainer[data-tt-id='" + ttId + "']");
  if (slideObj.length == 0) {
    return -1;
  }
  return $(".bxslider div.scrollContainer").index(slideObj);
};

// returns jQuery object for tr
function getTtIdTr(ttId) {
  return $("#script_table tbody tr[data-tt-id='" + ttId + "'");
};

// returns empty object if no tr is selected
function getSelectedTr() {
  return $("#script_table tbody tr.selected");
};

// returns empty object if no next tr exists
function getNextTr(trObject) {
  if (!trObject || trObject.length == 0) {
    throw new Error("null argument");
  }
  var nextInvisibleNodes = trObject.nextUntil("tr:visible");
  if (nextInvisibleNodes.length == 0) {
    return trObject.next("tr:visible");
  } else {
    return nextInvisibleNodes.last().next("tr:visible")
  }
};

// returns empty object if no previous tr exists
function getPrevTr(trObject) {
  if (!trObject || trObject.length == 0) {
    throw new Error("null argument");
  }
  var prevInvisibleNodes = trObject.prevUntil("tr:visible");
  if (prevInvisibleNodes.length == 0) {
    return trObject.prev("tr:visible");
  } else {
    return prevInvisibleNodes.last().prev("tr:visible")
  }
};

// do nothing if selected tr does not exist or next tr does not exist
// return true if selction is actually changed
function changeTrSelectionToNext() {
  var selected = getSelectedTr();
  if (selected.length == 0) {
    return false;
  }
  var next = getNextTr(selected);
  if (next.length == 0) {
    return false;
  }
  selectTr(next);
  return true;
};

// do nothing if selected tr does not exist or previous tr does not exist.
// return true if selction is actually changed
function changeTrSelectionToPrev() {
  var selected = getSelectedTr();
  if (selected.length == 0) {
    return false;
  }
  var prev = getPrevTr(selected);
  if (prev.length == 0) {
    return false;
  }
  selectTr(prev);
  return true;
};

// trObject..jQuery object for table tr element
function selectTr(trObject) {
  // clear all current selections
  $("tr.selected").removeClass("selected");
  trObject.addClass("selected");
};

// change current slide index to the index for the selected tr
function syncSlideIndexToSelectedTr() {
  var selected = getSelectedTr();
  if (selected.length == 0) {
    return;
  }
  var ttId = getTrTtId(selected);
  var slideIndex = getTtIdSlideIndex(ttId);
  if (slideIndex == -1) {
    slideIndex = getTtIdSlideIndex('noImage');
    if (slideIndex == -1) {
      throw new Error("noImage slide not found");
    }
  }
  slider.goToSlide(slideIndex);
};

// positive value means down direction scroll
function requiredScrollOffsetToShowTr(trObject) {
  var trTop = trObject.position().top;
  var trBottom = trTop + trObject.height();
  if (trTop < 0) {
    return trTop;
  } else if (trBottom > $("#script_table_container").height()) {
    return trBottom - $("#script_table_container").height();
  } else {
    return 0;
  }
};

function scrollToShowSelectedTr() {
  var selectedTr = getSelectedTr();
  if (selectedTr.length == 0) {
    return;
  }
  var scrollOffset =requiredScrollOffsetToShowTr(selectedTr);
  if (scrollOffset != 0) {
    $("#script_table_container").scrollTop(
      $("#script_table_container").scrollTop() + scrollOffset);
    $("#script_table_container").perfectScrollbar("update");
  }
};

function expandSelectedTr() {
  var selectedTr = getSelectedTr();
  if (selectedTr.length == 0) {
    return;
  }
  $("#script_table").treetable("expandNode", getTrTtId(selectedTr));
}

function collapseSelectedTr() {
  var selectedTr = getSelectedTr();
  if (selectedTr.length == 0) {
    return;
  }
  $("#script_table").treetable("collapseNode", getTrTtId(selectedTr));
}

// returns 0 if lineTtId == errLineTtId (means error line),
// returns positive if lineTtId > errLineTtId (means not executed),
// returns negative if lineTtd < errLineTtId (means already executed)
function compareToErrTtId(lineTtId, errLineTtId) {
  var lineArray = lineTtId.split("_");
  if (lineArray.length == 0) {
    throw new Error(lineTtId);
  }
  var errLineArray = errLineTtId.split("_");
  if (errLineArray.length == 0) {
    return -1; // already executed
  }
  
  for (var i = 0; i < lineArray.length; i++) {
    if (i >= errLineArray.length) { 
      throw new Error("lineTtId: " + lineTtId + "; errLineTtId: " + errLineTtId);
    }
    var line = parseInt(lineArray[i], 10);
    var errLine = parseInt(errLineArray[i], 10);
    if (line < errLine) {
      return -1; // already executed
    } else if (line > errLine) {
      return 1; // not executed
    }
  }
  return 0;
}

function getSrcTree() {
  if (srcTree == null) {
    var yamlObj = jsyaml.safeLoad(sahagin.srcTreeYamlStr);
    var tree = new sahagin.SrcTree();
    tree.fromYamlObject(yamlObj);
    tree.resolveKeyReference();
    // TODO should call SrcTreeChecker
    srcTree = tree;
  }
  return srcTree;
}

function getFunctionKey(code) {
  if (!(code instanceof sahagin.SubFunctionInvoke)) {
    return '';
  }
  var invoke = code;
  return invoke.getSubFunctionKey();
}

// add tr's codeBody node
function loadCodeBodyHiddenNode(tr) {
  var trFuncKey = tr.attr("data-func-key")
  if (trFuncKey == '') {
    return; // no need to load child nodes
  }

  var srcTree = getSrcTree();
  var trTtId = getTrTtId(tr);
  var testFunction = srcTree.getTestFunctionByKey(trFuncKey);
  if (testFunction.getCodeBody().length == 0) {
    return; // no need to load child nodes
  }
  var childNodeHtml = '';
  for (var i = 0; i < testFunction.getCodeBody().length; i++) {
    var codeLine = testFunction.getCodeBody()[i];
    var parentTtId = trTtId;
    var ttId = parentTtId + '_' + i.toString(10);
    var funcKey = getFunctionKey(codeLine.getCode());
    var errCompare = compareToErrTtId(ttId, sahagin.errLineTtId);
    var lineClass;
    if (errCompare == 0) {
      lineClass = "errorLine";
    } else if (errCompare > 0) {
      lineClass = "notRunLine";
    } else {
      lineClass = "successLine";
    }
    var pageTestDoc = sahagin.TestDocResolver.pageTestDoc(codeLine.getCode());
    if (pageTestDoc == null) {
      pageTestDoc = '-';
    }
    var testDoc = sahagin.TestDocResolver.placeholderResolvedFuncTestDoc(codeLine.getCode());
    if (testDoc == null) {
      testDoc = '';
    }
    var original = codeLine.getCode().getOriginal();
    childNodeHtml = childNodeHtml + sahagin.CommonUtils.strFormat(
      '<tr data-tt-id="{0}" data-tt-parent-id="{1}" data-func-key="{2}" class="{3}">'
          + '<td>{4}</td><td>{5}</td><td>{6}</td></tr>',
      ttId, parentTtId, funcKey, lineClass, pageTestDoc, testDoc, original);
  }
  
  var trNode = $("#script_table").treetable("node", trTtId);
  $("#script_table").treetable("loadBranch", trNode, childNodeHtml);
  // loadBranch automatically expand the trNode,
  // but this behavior is not desirable, so force collapse the trNode again
  $("#script_table").treetable("collapseNode", trTtId);
}

$(document).ready(function() {
  $("#script_table").treetable({
    expandable: true,
    onNodeExpand: function() {
      var selectedTr = getSelectedTr();
      if (selectedTr.length == 0) {
        return;
      }
      if (!selectedTr.hasClass("loaded")) {
        selectedTr.addClass("loaded");
      	var ttId = getTrTtId(selectedTr);
        var children = $("tr[data-tt-parent-id='" + ttId + "']");
        // TODO value of children.length may change after loadCodeBodyNode is called ??
        var childrenLength = children.length;
        for (var i = 0; i < childrenLength; i++) {
          loadCodeBodyHiddenNode(children.eq(i));
        }
      }
      // tree table size may change
      $("#script_table_container").perfectScrollbar("update");
    },
    onNodeCollapse: function() {
      // tree table size may change
      $("#script_table_container").perfectScrollbar("update");
    }
  });

  slider = $(".bxslider").bxSlider({
    speed: 1,
    infiniteLoop: false,
    hideControlOnEnd: true,
    pager: false,
    controls: false
  });

  $(".scrollContainer").perfectScrollbar({
    useKeyboard: false
  });

  $(document).on("mousedown", "#script_table tbody tr", function() {
    selectTr($(this));
    syncSlideIndexToSelectedTr();
  });

  // "#script_table body tr"
  $(document).keydown(function(e) {
    if (e.keyCode == "38") {
      // up key changes table line selection to next
      if (changeTrSelectionToPrev()) {
        syncSlideIndexToSelectedTr();
        scrollToShowSelectedTr();
      };
    } else if (e.keyCode == "40") {
      // down key changes table row selection to prev
      if (changeTrSelectionToNext()) {
        syncSlideIndexToSelectedTr();
        scrollToShowSelectedTr();
      };
    } else if (e.keyCode == "39") {
      // right key expands the selected node
      expandSelectedTr();
    } else if (e.keyCode == "37") {
      // right key collapse the selected node
      collapseSelectedTr();
    } else {
      return true;
    }
    return false;
  });

  // select table line for first capture
  var firstTtId = getSlideTtId(0);
  var firstTrObj = getTtIdTr(firstTtId);
  selectTr(firstTrObj);
});