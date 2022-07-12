package online.flowerinsnow.noimsecure.config;

public class NoImSecureConfig {
    public static class Secure {
        public static class NoPublicKey {
            public static boolean read = true;
            public static boolean write = true;
        }
    }

    public static class Render {
        public static class NoInsecure {
            public static boolean server = true;
            public static boolean chat = true;
        }
    }
}
