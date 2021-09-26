package de.melanx.easyskyblockmanagement.items;

import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import io.github.noeppi_noeppi.libx.base.ItemBase;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

// TODO remove
public class AllTeams extends ItemBase {

    public AllTeams(Properties properties) {
        super(EasySkyblockManagement.getInstance(), properties);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (level.isClientSide) {
            AllTeamsScreen.open();
        }

        return super.use(level, player, hand);
    }
}
