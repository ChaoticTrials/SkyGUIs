package de.melanx.easyskyblockmanagement;

import de.melanx.easyskyblockmanagement.items.AllTeams;
import de.melanx.easyskyblockmanagement.items.CreateTeam;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.world.item.Item;

@RegisterClass
public class ModItems {

    public static final Item allTeams = new AllTeams(new Item.Properties());
    public static final Item createTeam = new CreateTeam(new Item.Properties());
}
