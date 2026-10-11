package com.dbtenkaichi.platform;

import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public interface IRegistration {
    <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTabBuilder();
}
