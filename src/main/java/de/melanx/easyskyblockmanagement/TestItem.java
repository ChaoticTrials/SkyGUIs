package de.melanx.easyskyblockmanagement;

import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import io.github.noeppi_noeppi.libx.base.ItemBase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

// TODO remove
public class TestItem extends ItemBase {

    public TestItem(Properties properties) {
        super(EasySkyblockManagement.getInstance(), properties);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (level.isClientSide) {
            SkyblockSavedData data = SkyblockSavedData.get(level);
//            if (data.hasPlayerTeam(player)) {
//                player.sendMessage(new TranslatableComponent("You have a team"), UUID.randomUUID());
//            } else {
            Minecraft.getInstance().setScreen(new AllTeamsScreen());
//            }
        }

        return super.use(level, player, hand);
    }
}
