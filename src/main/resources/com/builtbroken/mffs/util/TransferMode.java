package com.builtbroken.mffs.util;

/**
 * The force field transfer mode.
 */
public enum TransferMode
{
    equalize, distribute, drain, fill;

    public TransferMode toggle()
    {
        return TransferMode.values()[ordinal() + 1 % TransferMode.values().length];
    }

    public static TransferMode get(int i)
    {
        return i >= 0 && i < values().length ? values()[i] : equalize;
    }
}