/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.immomo.litebuild.helper;

import com.immomo.litebuild.Constant;
import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.Log;
import com.immomo.litebuild.util.Utils;

public class IncrementPatchHelper {
    public boolean patchToApp() {
        if (!Settings.getData().hasClassChanged && !Settings.getData().hasResourceChanged) {
            Log.v(Constant.TAG, "No change, do nothing!!");
            return false;
        }

        Log.v(Constant.TAG, "No ------ !!" + Settings.getData().hasClassChanged + Settings.getData().hasResourceChanged);

        String cmds = new String();
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "adb shell am force-stop " + Settings.getPropertiesEnv().getProperty("debug_package");
        cmds += '\n' + "adb shell am start -n " + Settings.getPropertiesEnv().getProperty("debug_package") + "/" + Settings.getPropertiesEnv().getProperty("launcher_activity");
        Utils.runShell(cmds);

        return true;
    }

    public void patchDex() {

    }
}
