package de.melanx.easyskyblockmanagement;

import io.github.noeppi_noeppi.libx.base.ItemBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// TODO remove
public class TestItem<T extends Screen> extends ItemBase {

    private final Class<T> clazz;

    public TestItem(Class<T> clazz, Properties properties) {
        super(EasySkyblockManagement.getInstance(), properties);
        this.clazz = clazz;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (level.isClientSide) {
            try {
                Constructor<T> constructor = this.clazz.getConstructor();
                Minecraft.getInstance().setScreen(constructor.newInstance());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return super.use(level, player, hand);
    }
}
