package de.melanx.skyguis.client.screen.edit;

import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.base.list.ListScreen;
import de.melanx.skyguis.client.screen.base.list.PlayerListScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;

public class EditSpawnsScreen extends ListScreen<BlockPos> implements LoadingResultHandler {

    private final Team team;
    private final BaseScreen prev;
    private Button removeButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;
    private Runnable onSuccess;

    public EditSpawnsScreen(Team team, BaseScreen prev) {
        super(ComponentBuilder.text("remove_spawns"), team.getPossibleSpawns().stream().map(TemplatesConfig.Spawn::pos).toList(), 200, 230, new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.removeButton = this.addRenderableWidget(Button.builder(ComponentBuilder.text("remove"), (button -> {
                    List<BlockPos> positions = this.getSelectedValues().stream().toList();
                    Minecraft.getInstance().pushGuiLayer(new YouSureScreen(this, ComponentBuilder.text("you_sure_remove_spawns", positions.size()), () -> {
                        SkyGUIs.getNetwork().handleRemoveSpawns(new HashSet<>(positions));
                        this.onSuccess = () -> {
                            Minecraft.getInstance().setScreen(this.prev);
                        };
                    }));
                }))
                .bounds(this.x(10), this.y(200), 60, 20)
                .build());

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(77), this.y(200), 95, 20)
                .build());

        this.selectAll = this.addRenderableWidget(new SizeableCheckbox(this.x(9), this.y(32), 14, false, this.allSelected() ? UNSELECT_ALL : SELECT_ALL) {
            @Override
            public void onPress() {
                super.onPress();
                if (this.selected) {
                    EditSpawnsScreen.this.selectAll();
                } else {
                    EditSpawnsScreen.this.unselectAll();
                }
                EditSpawnsScreen.this.updateButtons();
            }
        });

        super.init();
        this.updateButtons();
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    @Override
    protected void fillWidgets() {
        for (int i = 0; i < this.values.size(); i++) {
            this.addCheckboxWidget(this.renderArea.addRenderableWidget2(new SpawnWidget(this.values.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12)));
        }
    }

    @Override
    public void render_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, ComponentBuilder.text("selected_amount", this.selectedAmount), this.x(28), this.y(35), Color.DARK_GRAY.getRGB(), false);
    }

    public void updateButtons() {
        List<BlockPos> selectedIds = this.getSelectedValues().stream().sorted().toList();
        this.removeButton.active = this.getSelectedValues().size() < this.getValues().size();
        this.selectAll.selected = this.allSelected();
        this.selectedAmount = selectedIds.size();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        this.updateButtons();
        return ret;
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        switch (result.status()) {
            case SUCCESS -> this.onSuccess.run();
            case FAIL -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
            }
        }
    }

    protected class SpawnWidget extends CheckboxTextWidget {

        public SpawnWidget(BlockPos value, Screen screen, int x, int y, int width, int height) {
            super(value, screen, x, y, width, height, Component.literal(value.getX() + " " + value.getY() + " " + value.getZ()));
        }
    }
}
