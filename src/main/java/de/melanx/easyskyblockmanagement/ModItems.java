package de.melanx.easyskyblockmanagement;

import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.world.item.Item;

@RegisterClass
public class ModItems {

    public static final Item test = new TestItem(new Item.Properties());
}
