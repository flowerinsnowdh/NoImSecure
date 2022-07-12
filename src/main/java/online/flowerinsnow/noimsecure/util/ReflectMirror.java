package online.flowerinsnow.noimsecure.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.lang.reflect.Field;

public class ReflectMirror {
    public static class ServerLoginNetworkHandler {
        public static class State {
            public static final Class<?> CLASS = ReflectMirror.getClass("net.minecraft.class_3248$class_3249");
            public static final Object READY_TO_ACCEPT = ReflectMirror.getStaticField(CLASS, "field_14168", "Lagr$b;");
            public static final Object ACCEPTED = ReflectMirror.getStaticField(CLASS, "field_14172", "Lagr$b;");
            public static final Object HELLO = ReflectMirror.getStaticField(CLASS, "field_14170", "Lagr$b;");
            public static final Object DELAY_ACCEPT = ReflectMirror.getStaticField(CLASS, "field_14171", "Lagr$b;");
            public static final Object AUTHENTICATING = ReflectMirror.getStaticField(CLASS, "field_14169", "Lagr$b;");
            public static final Object KEY = ReflectMirror.getStaticField(CLASS, "field_14175", "Lagr$b;");
            public static final Object NEGOTIATING = ReflectMirror.getStaticField(CLASS, "field_14173", "Lagr$b;");
        }
    }

    public static Class<?> getClass(String name) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        try {
            return Class.forName(resolver.mapClassName("intermediary", name));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getStaticField(Class<?> cls, String name, String descriptor) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        try {
            Field f = cls.getDeclaredField(resolver.mapFieldName(
                    "intermediary",
                    resolver.unmapClassName("intermediary", cls.getName()),
                    name, descriptor));
            f.setAccessible(true);
            return f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
