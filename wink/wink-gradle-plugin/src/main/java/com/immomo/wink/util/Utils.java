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

package com.immomo.wink.util;

import com.immomo.wink.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static List<String> runShell(String shStr) {
        return runShell(shStr, true);
    }

    public static List<String> runShell(String shStr, boolean outputError) {
        WinkLog.d("准备运行shell : " + shStr);
        List<String> strList = new ArrayList<String>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "`" + shStr + "`"}, null, null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                strList.add(line);
            }

            while (outputError && (line = errorReader.readLine()) != null) {
                WinkLog.w(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String str : strList) {
            System.out.print(str);
        }

        return strList;
    }

    public static void executeScript(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "`" + cmd + "`"}, null, null);
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            WinkLog.i(line);
        }

        while ((line = errorReader.readLine()) != null) {
            WinkLog.i(line);
        }
    }

    public static class ShellResult {
        List<String> result = new ArrayList<>();
        List<String> errorResult = new ArrayList<>();
        Exception e;

        public List<String> getResult() {
            return result;
        }

        public ShellResult setResult(List<String> result) {
            this.result = result;
            return this;
        }

        public List<String> getErrorResult() {
            return errorResult;
        }

        public ShellResult setErrorResult(List<String> errorResult) {
            this.errorResult = errorResult;
            return this;
        }

        public Exception getE() {
            return e;
        }

        public ShellResult setE(Exception e) {
            this.e = e;
            return this;
        }
    }

    public static ShellResult runShells(String... cmds) {
        return runShells(true, cmds);
    }

    public static ShellResult runShells(boolean reportLog, String... cmds) {
        ShellResult result = new ShellResult();

        List<String> cmdArray = new ArrayList<>();

        cmdArray.add("/bin/sh");
        cmdArray.add("-c");

        StringBuilder sb = new StringBuilder();
        for (String cmd: cmds) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(cmd);
            WinkLog.d("Execute shell: ", cmd);
        }
        cmdArray.add(sb.toString());

        try {
            Process process = Runtime.getRuntime().exec(cmdArray.toArray(new String[cmdArray.size()]));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            while (process.isAlive()) {
                process.waitFor(200, TimeUnit.MILLISECONDS);
                while ((line = reader.readLine()) != null) {
                    result.getResult().add(line);

                    if (reportLog) {
                        WinkLog.d("Shell result: ", line);
                    }
                }

                while ((line = errorReader.readLine()) != null) {
                    result.getErrorResult().add(line);

                    if (reportLog) {
                        WinkLog.w("Shell error: " + line);
                    }
                }
            }
        } catch (Exception e) {
            WinkLog.throwAssert("Shell exception:", e);
            result.setE(e);
        }

        return result;
    }

    /**
     * 获取单个文件的MD5值
     *
     * @param file  文件
     * @param radix 位 16 32 64
     * @return MD5
     */
    public static String getFileMD5s(File file, int radix) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(radix);
    }

    public static String upperCaseFirst(String val) {
        char[] arr = val.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }

    public static void copyFile(File source,File dest){
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void deleteDirChilds(String fileParent){
        File parent = new File(fileParent);
        if(parent.exists() && parent.isDirectory()){
            for(File file:parent.listFiles()){
                file.delete();
            }
        }
    }
}

