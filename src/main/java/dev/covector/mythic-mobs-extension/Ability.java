package dev.covector.maplus.mmextension;

public abstract class Ability {
    public abstract String getSyntax();

    public abstract String getId();

    public abstract String cast(String[] args);
}