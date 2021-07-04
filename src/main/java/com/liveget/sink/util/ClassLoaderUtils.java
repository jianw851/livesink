package com.liveget.sink.util;

import com.liveget.sink.parser.Pricing;
import com.liveget.sink.parser.Signal;
import java.lang.reflect.InvocationTargetException;

public class ClassLoaderUtils {

    public static Pricing instantiatePricingClassByName(String pricingSourceName) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        String className = ClassLoaderUtils.getClassName(pricingSourceName.split("-")[0], "Pricing");
        Class<Pricing> theClass = (Class<Pricing>)Class.forName("com.liveget.sink.parser." + className);
        Pricing ret = theClass.getDeclaredConstructor().newInstance();
        return ret;
    }

    public static Signal instantiateSignalClassByName(String signalSourceName) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        String className = ClassLoaderUtils.getClassName(signalSourceName.split("-")[0], "Signal");
        Class<Signal> theClass = (Class<Signal>)Class.forName("com.liveget.sink.parser." + className);
        Signal ret = theClass.getDeclaredConstructor().newInstance();
        return ret;
    }

    public static String getClassName(String source, String prefix) {
        StringBuilder sb = new StringBuilder();
        if(prefix != null)
            sb.append(prefix);
        boolean initial = false;
        for(int i = 0; i < source.length(); ++i) {
            if(!initial) {
                if (source.charAt(i) >= 'a' && source.charAt(i) <= 'z') {
                    sb.append((char) ((int) source.charAt(i) - 32));
                    initial = true;
                } else if (source.charAt(i) >= 'A' && source.charAt(i) <= 'Z') {
                    sb.append(source.charAt(i));
                    initial = true;
                }
            } else {
                if (source.charAt(i) >= 'a' && source.charAt(i) <= 'z') {
                    sb.append(source.charAt(i));
                } else if (source.charAt(i) >= 'A' && source.charAt(i) <= 'Z') {
                    sb.append((char) ((int) source.charAt(i) + 32));
                }
            }
        }
        return sb.toString();
    }
}
