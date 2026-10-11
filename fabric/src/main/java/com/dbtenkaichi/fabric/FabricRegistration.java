package com.dbtenkaichi.fabric;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.platform.IRegistration;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public class FabricRegistration implements IRegistration {
    @Override
    public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        T value = Registry.register(registry, Tenkaichi.id(name), factory.get());
        return () -> value;
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricCreativeModeTab.builder();
    }
}
