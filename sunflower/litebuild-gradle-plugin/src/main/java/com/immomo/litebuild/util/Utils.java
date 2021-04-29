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

package com.immomo.litebuild.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    /**
     * 运行shell并获得结果，注意：如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流
     *
     * @param shStr 需要执行的shell
     * @return
     */
    public static List<String> runShell(String shStr) {
//        System.out.println("准备运行shell : " + shStr);
        List<String> strList = new ArrayList<String>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "`" + shStr + "`"}, null, null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                strList.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String str : strList) {
            System.out.print(str);
        }
//        System.out.println("结束运行shell : " + shStr);
        return strList;
    }

    public static void executeScript(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));


        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        line = "";
        while ((line = errorReader.readLine()) != null) {
            System.out.println(line);
        }
    }
//    /**
//     * 运行shell并获得结果，注意：如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流
//     *
//     * @param
//     * @return
//     */
//    public static List<String> runShell(String... cmds) {
//        List<String> strList = new ArrayList<>();
//        List<String> cmdArray = new ArrayList<>();
//
//        cmdArray.add("/bin/sh");
//        cmdArray.add("-c");
//        for (String cmd: cmdArray) {
//            cmdArray.add("`" + cmd + "`");
//        }
//
//        try {
//            Process process = Runtime.getRuntime().exec((String[]) cmdArray.toArray());
//            InputStreamReader ir = new InputStreamReader(process.getInputStream());
//            LineNumberReader input = new LineNumberReader(ir);
//            String line;
//            process.waitFor();
//            while ((line = input.readLine()) != null){
//                strList.add(line);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return strList;
//    }
}
