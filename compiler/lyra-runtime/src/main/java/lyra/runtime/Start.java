package lyra.runtime;

import java.io.File;
import java.lang.*;
import java.lang.String;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 *
 */
public class Start {
    private static int nextId = 1;
    public static Object lyra_null = new Null();
    public static Bool lyra_false = Bool._false;
    public static Bool lyra_true = Bool._true;
    public static Output lyra_out = new StandardOutput();
    public static Input lyra_in = new StandardInput();

    public static int makeId() {
        return nextId++;
    }

    public static void main(java.lang.String[] args) {
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            if (args.length > 0) {
                try {
                    loader = new URLClassLoader(new URL[] {
                            new File(args[0]).toURI().toURL()
                    });
                } catch (MalformedURLException e) {
                    System.err.println("Could not produce a URL from \"" +
                                        args[0] + "\".");
                    e.printStackTrace();
                    return;
                }
            }
            initStatic(loader);
            callMain(loader);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static void initStatic(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> init;
        try {
            init = loader.loadClass("lyra.user.StaticInitializer");
        } catch (ClassNotFoundException e) {
            /* no static initialization required */
            return;
        }

        Method staticInit;
        try {
            staticInit = init.getMethod("staticInit");
        } catch (NoSuchMethodException e) {
            System.err.println("No staticInit on StaticInitializer, compiler bug?");
            throw e;
        }
        staticInit.invoke(null);
    }

    private static void callMain(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> appClass;
        try {
            appClass = loader.loadClass("lyra.user.Application");
        } catch (ClassNotFoundException e) {
            System.err.println("No user-defined Application class found!");
            throw e;
        }

        Constructor<?> constructor;
        try {
            constructor = appClass.getConstructor();
        } catch (NoSuchMethodException e) {
            System.err.println("User-defined Application class has no constructor with " +
                    "no arguments!");
            throw e;
        }

        java.lang.Object app = constructor.newInstance();
        Method main;
        try {
            main = appClass.getMethod("lyra_main");
        } catch (NoSuchMethodException e) {
            System.err.println("User-defined Application class has no main method " +
                               "without arguments!");
            throw e;
        }

        main.invoke(app);
    }
}
