package com.barma.udk.gui;

import android.content.Context;
import com.barma.udk.core.AesCipher;
import com.barma.udk.core.interfaces.ICipher;
import com.barma.udk.core.interfaces.IConfigurator;
import com.barma.udk.core.interfaces.IStorage;
import com.barma.udk.database.DbStorage;

/**
 * Created by Vitalii Misiura on 12/18/14.
 * Implementation of IConfigurator interface
 */
public class Configurator implements IConfigurator {

    public Configurator(Context context){
        m_context = context;
    }

    @Override
    public ICipher createDefaultCipher(final String password){
        return new AesCipher(password);
    }

    @Override
    public IStorage createDefaultStorage(){
        return new DbStorage(m_context);
    }

    private static Context m_context;
}
