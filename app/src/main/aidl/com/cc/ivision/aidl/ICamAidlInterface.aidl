// ICamAidlInterface.aidl
package aidl.com.cc.ivision.aidl;

// Declare any non-default types here with import statements

interface ICamAidlInterface {


    void setSwitch(boolean isOn);

    boolean getSwitch();

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


}