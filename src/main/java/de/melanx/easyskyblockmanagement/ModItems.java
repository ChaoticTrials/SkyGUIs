package de.melanx.easyskyblockmanagement;

import de.melanx.easyskyblockmanagement.client.screen.CreateTeamScreen;
import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.world.item.Item;

@RegisterClass
public class ModItems {

    public static final Item allTeams = new TestItem<>(AllTeamsScreen.class, new Item.Properties());
    public static final Item createTeam = new TestItem<>(CreateTeamScreen.class, new Item.Properties());
}
