package com.barma.udk.core.interfaces;

/**
 * Created by Vitalii Misiura on 11/7/14.
 */
public interface ICipher
{
    /** Encrypt value
     * */
    public String encrypt(String value);

    /** Decrypt value
     * */
    public String decrypt(String value);

    /** Returns hash of cipher password*/
    public String getPasswordHash();
}
