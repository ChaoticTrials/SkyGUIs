package de.melanx.easyskyblockmanagement.client.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.TextHelper;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.edit.InvitablePlayersScreen;
import de.melanx.easyskyblockmanagement.client.screen.edit.TeamPlayersScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.InformationScreen;
import de.melanx.easyskyblockmanagement.client.widget.BlinkingEditBox;
import de.melanx.easyskyblockmanagement.client.widget.LoadingCircle;
import de.melanx.easyskyblockmanagement.network.handler.EditSpawns;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

public class TeamEditScreen extends BaseScreen {

    private static final Component SPAWNS = ComponentBuilder.text("spawns");
    private static final Component MEMBERS = ComponentBuilder.text("members");
    private static final Component INVALID = ComponentBuilder.text("invalid");
    private static final Component ADD = ComponentBuilder.text("add");
    private static final Component REMOVE = ComponentBuilder.text("remove");
    private static final Component SHOW = ComponentBuilder.text("show");
    private static final Component INVITE = ComponentBuilder.text("invite");

    private final Team team;
    private final BaseScreen prev;
    private final Random random;
    private BlinkingEditBox posBox;
    private Button addButton;
    private Button removeButton;
    private boolean posValid;

    public TeamEditScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), 245, 185);
        this.team = team;
        this.prev = prev;
        this.random = new Random();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.x(10), this.y(45), 70, 20, SHOW, button -> {
            for (BlockPos spawn : this.team.getPossibleSpawns()) {
                double posX = spawn.getX() + 0.5D;
                double posY = spawn.getY() + 0.5D;
                double posZ = spawn.getZ() + 0.5D;

                // [Vanilla copy]
                // net.minecraft.client.multiplayer.ClientPacketListener#handleParticleEvent(ClientboundLevelParticlesPacket)
                for (int i = 0; i < 10; i++) {
                    double offsetX = this.random.nextGaussian() * 0.1;
                    double offsetY = this.random.nextGaussian() * 0.1;
                    double offsetZ = this.random.nextGaussian() * 0.1;
                    double speedX = this.random.nextGaussian() * 10;
                    double speedY = this.random.nextGaussian() * 10;
                    double speedZ = this.random.nextGaussian() * 10;

                    //noinspection ConstantConditions
                    this.minecraft.level.addParticle(ParticleTypes.HAPPY_VILLAGER, false, posX + offsetX, posY + offsetY, posZ + offsetZ, speedX, speedY, speedZ);
                }
            }
            this.onClose();
        }));

        this.addButton = this.addRenderableWidget(new Button(this.x(85), this.y(45), 70, 20, ADD, button -> {
            BlockPos pos = this.getPos();
            if (this.posValid && pos != null) {
                EasySkyblockManagement.getNetwork().handleEditSpawns(EditSpawns.Type.ADD, pos);
                //noinspection ConstantConditions
                this.getLoadingCircle().setActive(true);
            }
        }, (button, poseStack, mouseX, mouseY) -> this.posBox.blink()));

        //noinspection ConstantConditions
        Vec3 pos = Minecraft.getInstance().player.position();
        String posStr = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        this.posBox = new BlinkingEditBox(this.font, this.x(10), this.y(70), 145, 18, new TextComponent(posStr));
        this.posBox.setValue(posStr);
        this.posBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.posBox);

        this.removeButton = this.addRenderableWidget(new Button(this.x(160), this.y(45), 70, 20, REMOVE, button -> {
            BlockPos removePos = this.getPos();
            if (this.posValid && removePos != null) {
                EasySkyblockManagement.getNetwork().handleEditSpawns(EditSpawns.Type.REMOVE, removePos);
                //noinspection ConstantConditions
                this.getLoadingCircle().setActive(true);
            }
        }));

        this.addRenderableWidget(new Button(this.x(10), this.y(115), 70, 20, SHOW, button -> {
            Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this));
        }));

        this.addRenderableWidget(new Button(this.x(85), this.y(115), 70, 20, INVITE, button -> {
            Minecraft.getInstance().setScreen(new InvitablePlayersScreen(this.team, this));
        }));

        this.addRenderableWidget(new Button(this.x(10), this.y(155), 226, 20, PREV_SCREEN_COMPONENT, button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));

        if (!ConfigHandler.Utility.selfManage || !ConfigHandler.Utility.Spawns.modifySpawns) {
            this.addButton.active = false;
            this.removeButton.active = false;
        }
    }

    @Nullable
    @Override
    public LoadingCircle createLoadingCircle() {
        return new LoadingCircle(this.centeredX(32), this.centeredY(32), 32);
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        Minecraft minecraft = Minecraft.getInstance();
        ForgeHooksClient.pushGuiLayer(minecraft, new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, () -> {
            ForgeHooksClient.popGuiLayer(minecraft);
        }));
    }

    @Override
    public void tick() {
        this.posBox.tick();
        this.updatePositionValidation();
        super.tick();
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);

        this.font.draw(poseStack, SPAWNS, this.x(10), this.y(30), Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, MEMBERS, this.x(10), this.y(100), Color.DARK_GRAY.getRGB());

        if (!this.posValid) {
            poseStack.pushPose();
            poseStack.scale(1.3F, 1.3F, 1.3F);
            this.font.draw(poseStack, INVALID, (float) ((this.x(191) - this.font.width(INVALID) / 2) / 1.3), (float) ((this.y(74)) / 1.3), Color.RED.getRGB());
            poseStack.popPose();
        }
    }

    private void updatePositionValidation() {
        String posBoxValue = this.posBox.getValue();
        String[] args = posBoxValue.split(" ");
        //noinspection ConstantConditions
        this.posValid = args.length == 3
                && Arrays.stream(args).allMatch(s -> {
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        })
                && Integer.parseInt(args[1]) >= Minecraft.getInstance().level.getMinBuildHeight()
                && Integer.parseInt(args[1]) <= Minecraft.getInstance().level.getMaxBuildHeight();

        if (this.posValid) {
            this.posBox.setValid();
            if (ConfigHandler.Utility.selfManage && ConfigHandler.Utility.Spawns.modifySpawns) {
                this.addButton.active = true;
                this.removeButton.active = true;
            }
        } else {
            this.posBox.setInvalid();
            this.addButton.active = false;
            this.removeButton.active = false;
        }
    }

    @Nullable
    private BlockPos getPos() {
        if (this.posValid) {
            String value = this.posBox.getValue();
            String[] args = value.split(" ");
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            return new BlockPos(x, y, z);
        }

        return null;
    }
}
