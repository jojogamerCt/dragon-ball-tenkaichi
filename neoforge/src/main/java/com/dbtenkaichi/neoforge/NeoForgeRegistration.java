package com.dbtenkaichi.neoforge;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.platform.IRegistration;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgeRegistration implements IRegistration {
    private static final Map<Registry<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry, r -> DeferredRegister.create(registry, Tenkaichi.MOD_ID));
        return register.register(name, factory);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    public static void attach(IEventBus bus) {
        REGISTERS.values().forEach(register -> register.register(bus));
    }
}
