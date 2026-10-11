package com.dbtenkaichi.platform;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatform PLATFORM = load(IPlatform.class);
    public static final INetwork NETWORK = load(INetwork.class);
    public static final IRegistration REGISTRY = load(IRegistration.class);

    private Services() {
    }

    public static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing platform service " + type.getName()));
    }
}
