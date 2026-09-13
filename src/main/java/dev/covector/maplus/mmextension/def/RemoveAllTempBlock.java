package dev.covector.maplus.mmextension.def;

import dev.covector.maplus.mmapihook.mechanics.TempBlockManager;
import dev.covector.maplus.mmextension.Ability;

public class RemoveAllTempBlock extends Ability {
    private String id = "removeAllTempBlock";
    private String syntax = "";

    public String cast(String[] args) {
        TempBlockManager.removeAllBlocks();
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
