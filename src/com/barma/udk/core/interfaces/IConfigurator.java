package com.barma.udk.core.interfaces;

/**
 * Created by Vitalii Misiura on 12/18/14.
 */
public interface IConfigurator {
    public ICipher createDefaultCipher(String password);
    public IStorage createDefaultStorage();
}
