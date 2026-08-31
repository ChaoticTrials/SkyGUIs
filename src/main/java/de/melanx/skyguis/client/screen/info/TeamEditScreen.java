package de.melanx.skyguis.client.screen.info;

import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.edit.EditSpawnsScreen;
import de.melanx.skyguis.client.screen.edit.InvitablePlayersScreen;
import de.melanx.skyguis.client.screen.edit.TeamPlayersScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.client.widget.ValidatingEditBox;
import de.melanx.skyguis.network.handler.EditSpawns;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import de.melanx.skyguis.util.ToggleButtons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TeamEditScreen extends BaseScreen {

    private static final Component SPAWNS = ComponentBuilder.text("spawns");
    private static final Component MEMBERS = ComponentBuilder.text("members");
    private static final Component REQUESTS = ComponentBuilder.text("requests");
    private static final Component ADD = ComponentBuilder.text("add");
    private static final Component REMOVE = ComponentBuilder.text("remove");
    private static final Component SHOW = ComponentBuilder.text("show");
    private static final Component INVITE = ComponentBuilder.text("invite");
    private static final Component ALLOW_VISITS = ComponentBuilder.text("allow_visits");
    private static final Component ALLOW_REQUESTS = ComponentBuilder.text("allow_requests");
    private static final MutableComponent VISIT_BASE = ComponentBuilder.button("allow_visits").append(Component.literal(" "));
    private static final MutableComponent REQUEST_BASE = ComponentBuilder.button("allow_requests").append(Component.literal(" "));

    private static final Component ALLOWED = ComponentBuilder.text("allowed").withStyle(ChatFormatting.GREEN);
    private static final Component DISALLOWED = ComponentBuilder.text("disallowed").withStyle(ChatFormatting.RED);

    private static final int LEFT_PADDING = 10;
    private static final int CHECKBOX_X = LEFT_PADDING + Math.max(TextHelper.stringLength(ALLOW_VISITS), TextHelper.stringLength(ALLOW_REQUESTS));

    private final Team team;
    private final BaseScreen prev;
    private final Random random;
    private ValidatingEditBox posBox;
    private Button addButton;
    private Button removeButton;
    private boolean posValid;

    public TeamEditScreen(Team team, BaseScreen prev) {
        super(Component.literal(team.getName()), 245, 220);
        this.team = team;
        this.prev = prev;
        this.random = new Random();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(SHOW, button -> {
                    assert this.minecraft.player != null;
                    if (!this.team.getPossibleSpawns().isEmpty()) {
                        this.minecraft.player.sendSystemMessage(SkyComponents.INFO_SHOW_TEAM_SPAWNS.apply(this.team.getName()));
                    }

                    for (TemplatesConfig.Spawn spawn : this.team.getPossibleSpawns()) {
                        BlockPos pos = spawn.pos();
                        double posX = pos.getX() + 0.5D;
                        double posY = pos.getY() + 0.5D;
                        double posZ = pos.getZ() + 0.5D;

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
                            this.minecraft.level.addParticle(ParticleTypes.HAPPY_VILLAGER, true, true,
                                    posX + offsetX,
                                    posY + offsetY,
                                    posZ + offsetZ,
                                    speedX, speedY, speedZ);
                        }

                        this.minecraft.player.sendSystemMessage(Component.literal(" - ").append(RandomUtility.getFormattedPos(pos)));
                    }
                    this.onClose();
                })
                .bounds(this.x(LEFT_PADDING), this.y(45), 70, 20)
                .build());

        CycleButton<WorldUtil.SpawnDirection> directionButton = this.addRenderableWidget(CycleButton.builder(TeamEditScreen::componentFromDirection, WorldUtil.SpawnDirection.SOUTH)
                .withValues(WorldUtil.SpawnDirection.values())
                .displayOnlyValue()
                .create(this.x(160), this.y(70), 70, 20, Component.empty()));

        this.addButton = this.addRenderableWidget(Button.builder(ADD, button -> {
                    BlockPos pos = this.getPos();
                    if (this.posValid && pos != null) {
                        SkyGUIs.getNetwork().handleEditSpawns(EditSpawns.Type.ADD, pos, directionButton.getValue());
                    }
                })
                .bounds(this.x(85), this.y(45), 70, 20)
                .build());

        this.removeButton = this.addRenderableWidget(Button.builder(REMOVE, button -> Minecraft.getInstance().setScreen(new EditSpawnsScreen(this.team, this)))
                .tooltip(Tooltip.create(BaseScreen.OPEN_NEW_SCREEN))
                .bounds(this.x(160), this.y(45), 70, 20)
                .build());

        // The pos box needs to be initialized after the other widgets
        // Otherwise, it will crash since EditBox#setValue will result in a call of TeamEditScreen#updatePositionValidation which requires the widgets being not null
        //noinspection ConstantConditions
        Vec3 pos = Minecraft.getInstance().player.position();
        String posStr = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        this.posBox = new ValidatingEditBox(this.font, this.x(LEFT_PADDING), this.y(71), 145, 18, Component.literal(posStr), this::updatePositionValidation);
        this.posBox.setValue(posStr);
        this.posBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.posBox);

        this.addRenderableWidget(Button.builder(SHOW, button -> Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this)))
                .bounds(this.x(LEFT_PADDING), this.y(115), 90, 20)
                .build());

        this.addRenderableWidget(Button.builder(INVITE, button -> Minecraft.getInstance().setScreen(new InvitablePlayersScreen(this.team, this)))
                .bounds(this.x(105), this.y(115), 90, 20)
                .build());

        this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X + 5), this.y(147), 10, this.team.allowsVisits(), VISIT_BASE.copy().append(this.team.allowsVisits() ? ALLOWED : DISALLOWED), (checkbox, value) -> ToggleButtons.toggleState(TeamEditScreen.this.team, checkbox, VISIT_BASE, ToggleButtons.Type.VISITS)));

        this.addRenderableWidget(new SizeableCheckbox(this.x(CHECKBOX_X + 5), this.y(162), 10, this.team.allowsJoinRequests(), REQUEST_BASE.copy().append(this.team.allowsJoinRequests() ? ALLOWED : DISALLOWED), (checkbox, value) -> ToggleButtons.toggleState(TeamEditScreen.this.team, checkbox, REQUEST_BASE, ToggleButtons.Type.JOIN_REQUEST)));

        this.addRenderableWidget(Button.builder(ComponentBuilder.button("leave_team"), button -> {
                    Minecraft.getInstance().pushGuiLayer(
                            new YouSureScreen(this, List.of(ComponentBuilder.text("you_sure_leave0"), ComponentBuilder.text("you_sure_leave1")), () -> {
                                SkyGUIs.getNetwork().leaveTeam(Minecraft.getInstance().player);
                            }));
                })
                .bounds(this.x(CHECKBOX_X + 25), this.y(150), 90, 20)
                .build());

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(LEFT_PADDING), this.y(this.ySize - 30), 226, 20)
                .build());

        if (!PermissionManager.INSTANCE.hasPermission(Minecraft.getInstance().player, PermissionManager.Permission.EDIT_SPAWNS)) {
            this.addButton.active = false;
            this.removeButton.active = false;
        }
    }

    private static Component componentFromDirection(WorldUtil.SpawnDirection direction) {
        return switch(direction) {
            case NORTH -> ComponentBuilder.text("north");
            case SOUTH -> ComponentBuilder.text("south");
            case EAST -> ComponentBuilder.text("east");
            case WEST -> ComponentBuilder.text("west");
        };
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        this.renderTitle(graphics);

        graphics.text(this.font, SPAWNS, this.x(LEFT_PADDING), this.y(30), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, MEMBERS, this.x(LEFT_PADDING), this.y(100), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, ALLOW_VISITS, this.x(LEFT_PADDING), this.y(149), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, ALLOW_REQUESTS, this.x(LEFT_PADDING), this.y(164), Color.DARK_GRAY.getRGB(), false);

        this.posBox.updateOutlineRendering(this.addButton.isHovered);
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
                && Integer.parseInt(args[1]) >= Minecraft.getInstance().level.getMinY()
                && Integer.parseInt(args[1]) <= Minecraft.getInstance().level.getMaxY()
                && !this.team.getPossibleSpawns().stream().map(TemplatesConfig.Spawn::pos).collect(Collectors.toSet()).contains(new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])));

        if (this.posValid) {
            this.posBox.setValid();
            if (PermissionManager.INSTANCE.hasPermission(Minecraft.getInstance().player, PermissionManager.Permission.EDIT_SPAWNS)) {
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
