package com.immomo.litebuild;

import java.io.Serializable;

public class LitebuildOptions implements Serializable {
    public String[] moduleWhitelist;
    public String[] moduleBlacklist;
    public boolean kotlinSyntheticsEnable;
}