package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.utf8Character;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

//This is not a test which checks some thing.
//This is input file for SrcTreeGeneratorTest
@TestDoc("Doc:メインクラス")
public class メインクラス {

    @TestDoc("Doc:テスト")
    @Test
    public void テスト() {
        ページクラス page = new ページクラス();
        page.ページメソッド();
    }

}
