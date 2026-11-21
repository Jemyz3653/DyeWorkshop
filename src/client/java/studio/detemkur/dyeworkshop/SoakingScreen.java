package studio.detemkur.dyeworkshop;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

final class SoakingScreen extends AbstractContainerScreen<SoakingMenu> {
    SoakingScreen(SoakingMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title,176,192);inventoryLabelY=84;
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff393841);
        graphics.fill(leftPos+2,topPos+2,leftPos+imageWidth-2,topPos+imageHeight-2,0xffd2c4ac);
        for (var slot:menu.slots) {
            graphics.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff443d38);
            graphics.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff9b8b75);
        }
        graphics.text(font,Component.translatable("dyeworkshop.instructions"),leftPos+8,topPos+28,0xff463b30,false);
        graphics.text(font,Component.translatable("dyeworkshop.dry_hint"),leftPos+8,topPos+73,0xff463b30,false);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int mouseX,int mouseY) {
        graphics.text(font,title,titleLabelX,titleLabelY,0xff30251c,false);
        graphics.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xff30251c,false);
    }
}
