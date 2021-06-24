package com.immomo.wink;

import com.immomo.wink.helper.ResourceHelper;
import com.immomo.wink.helper.CompileHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.IncrementPatchHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.util.WinkLog;
import com.immomo.wink.util.Utils;

import org.junit.Test;

public class JavaEntranceTest {

    @Test
    public void test() {
        int i = 0;
        JavaEntrance.main(new String[]{"../"});
        System.out.println("xx");
    }
}
