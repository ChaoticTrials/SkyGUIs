package de.melanx.easyskyblockmanagement.client.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.widget.BlinkingEditBox;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

public class TeamEditScreen extends BaseScreen {

    private final Team team;
    private final BaseScreen prev;
    private final Random random;
    private BlinkingEditBox posBox;
    private Button addButton;
    private boolean posValid;

    public TeamEditScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), 245, 185);
        this.team = team;
        this.prev = prev;
        this.random = new Random();
    }

    // TODO TranslatableTextComponent
    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.x(10), this.y(45), 70, 20, new TextComponent("Show"), button -> {
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

                System.out.println(spawn);
            }
            this.onClose();
        }));

        // TODO check for config
        this.addButton = new Button(this.x(85), this.y(45), 70, 20, new TextComponent("Add"), button -> {
            if (this.posValid) {
                // TODO networking
                this.onClose();
            }
        }, (button, poseStack, mouseX, mouseY) -> {
            this.posBox.blink();
            if (!button.active) {
                this.renderTooltip(poseStack, new TextComponent("Position is invalid!").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        });
        this.addRenderableWidget(this.addButton);

        //noinspection ConstantConditions
        Vec3 pos = Minecraft.getInstance().player.position();
        String posStr = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        this.posBox = new BlinkingEditBox(this.font, this.x(10), this.y(70), 145, 18, new TextComponent(posStr));
        this.posBox.setValue(posStr);
        this.posBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.posBox);

        // TODO check for config
        this.addRenderableWidget(new Button(this.x(160), this.y(45), 70, 20, new TextComponent("Remove"), button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));

        this.addRenderableWidget(new Button(this.x(10), this.y(115), 70, 20, new TextComponent("Show"), button -> {
            Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this));
        }));

        this.addRenderableWidget(new Button(this.x(85), this.y(115), 70, 20, new TextComponent("Invite"), button -> {
            // TODO open list with invitable users
        }));

        this.addRenderableWidget(new Button(this.x(10), this.y(155), 226, 20, PREV_SCREEN_COMPONENT, button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));
    }

    @Override
    public void tick() {
        this.posBox.tick();
        this.updatePositionValidation();
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);

        // TODO TranslatableTextComponent
        this.font.draw(poseStack, "Spawns", this.x(10), this.y(30), Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, "Members", this.x(10), this.y(100), Color.DARK_GRAY.getRGB());

        if (!this.posValid) {
            poseStack.pushPose();
            poseStack.scale(1.3F, 1.3F, 1.3F);
            this.font.draw(poseStack, "INVALID", (float) ((this.x(191) - this.font.width("INVALID") / 2) / 1.3), (float) ((this.y(74)) / 1.3), Color.RED.getRGB());
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
            this.addButton.active = true;
        } else {
            this.posBox.setInvalid();
            this.addButton.active = false;
        }
    }
}
