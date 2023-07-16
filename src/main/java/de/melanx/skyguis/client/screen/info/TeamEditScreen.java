package de.melanx.skyguis.client.screen.info;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.edit.InvitablePlayersScreen;
import de.melanx.skyguis.client.screen.edit.TeamPlayersScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.widget.BlinkingEditBox;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.network.handler.EditSpawns;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

public class TeamEditScreen extends BaseScreen implements LoadingResultHandler {

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
        super(Component.literal(team.getName()), 245, 185);
        this.team = team;
        this.prev = prev;
        this.random = new Random();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(SHOW, button -> {
                    for (TemplatesConfig.Spawn spawn : this.team.getPossibleSpawns()) {
                        double posX = spawn.pos().getX() + 0.5D;
                        double posY = spawn.pos().getY() + 0.5D;
                        double posZ = spawn.pos().getZ() + 0.5D;

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
                            this.minecraft.level.addParticle(ParticleTypes.HAPPY_VILLAGER, false,
                                    posX + offsetX,
                                    posY + offsetY,
                                    posZ + offsetZ,
                                    speedX, speedY, speedZ);
                        }
                    }
                    this.onClose();
                })
                .bounds(this.x(10), this.y(45), 70, 20)
                .build());

        this.addButton = this.addRenderableWidget(Button.builder(ADD, button -> {
                    BlockPos pos = this.getPos();
                    if (this.posValid && pos != null) {
                        SkyGUIs.getNetwork().handleEditSpawns(EditSpawns.Type.ADD, pos, Direction.SOUTH); // todo direction by button?
                        //noinspection ConstantConditions
                        this.getLoadingCircle().setActive(true);
                    }
                })
                .bounds(this.x(85), this.y(45), 70, 20)
                .build());

        //noinspection ConstantConditions
        Vec3 pos = Minecraft.getInstance().player.position();
        String posStr = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        this.posBox = new BlinkingEditBox(this.font, this.x(10), this.y(70), 145, 18, Component.literal(posStr));
        this.posBox.setValue(posStr);
        this.posBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.posBox);

        this.removeButton = this.addRenderableWidget(Button.builder(REMOVE, button -> {
                    BlockPos removePos = this.getPos();
                    if (this.posValid && removePos != null) {
                        SkyGUIs.getNetwork().handleEditSpawns(EditSpawns.Type.REMOVE, removePos, null);
                        //noinspection ConstantConditions
                        this.getLoadingCircle().setActive(true);
                    }
                })
                .bounds(this.x(160), this.y(45), 70, 20)
                .build());

        this.addRenderableWidget(Button.builder(SHOW, button -> Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this)))
                .bounds(this.x(10), this.y(115), 70, 20)
                .build());

        this.addRenderableWidget(Button.builder(INVITE, button -> Minecraft.getInstance().setScreen(new InvitablePlayersScreen(this.team, this)))
                .bounds(this.x(85), this.y(115), 70, 20)
                .build());

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(10), this.y(155), 226, 20)
                .build());

        if (!PermissionsConfig.selfManage || !PermissionsConfig.Spawns.modifySpawns) {
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
        minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
    }

    @Override
    public void tick() {
        this.posBox.tick();
        this.updatePositionValidation();
        super.tick();
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTitle(guiGraphics);

        guiGraphics.drawString(this.font, SPAWNS, this.x(10), this.y(30), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, MEMBERS, this.x(10), this.y(100), Color.DARK_GRAY.getRGB(), false);

        if (!this.posValid) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.3F, 1.3F, 1.3F);
            guiGraphics.drawString(this.font, INVALID.getString(), (float) ((this.x(191) - this.font.width(INVALID) / 2) / 1.3), (float) ((this.y(74)) / 1.3), Color.RED.getRGB(), false);
            guiGraphics.pose().popPose();
        }

        if (this.addButton.isHovered()) {
            this.posBox.blink();
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
            if (PermissionsConfig.selfManage && PermissionsConfig.Spawns.modifySpawns) {
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
