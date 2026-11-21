package studio.detemkur.dyeworkshop;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class SoakingMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ResultContainer output=new ResultContainer();
    private boolean consuming;
    public final Container input=new SimpleContainer(3) {
        @Override public void setChanged() { super.setChanged(); SoakingMenu.this.slotsChanged(this); }
    };
    public SoakingMenu(int id,Inventory inventory) { this(id,inventory,ContainerLevelAccess.NULL); }
    public SoakingMenu(int id,Inventory inventory,ContainerLevelAccess access) {
        super(DyeWorkshop.MENU,id);this.access=access;
        addSlot(new Slot(output,0,142,48) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean isFake() { return true; }
            @Override public void onTake(Player player,ItemStack stack) { consume(player); }
        });
        for (int i=0;i<3;i++) {
            final int ingredient=i;
            addSlot(new Slot(input,i,22+i*34,48) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return switch(ingredient) {
                        case 0 -> DyeWorkshop.armor(stack);
                        case 1 -> stack.has(DataComponents.DYE);
                        default -> stack.is(Items.WATER_BUCKET);
                    };
                }
            });
        }
        addStandardInventorySlots(inventory,8,96);
    }
    private boolean valid() {
        return DyeWorkshop.armor(input.getItem(0)) && input.getItem(1).has(DataComponents.DYE)
            && input.getItem(2).is(Items.WATER_BUCKET);
    }
    @Override public void slotsChanged(Container container) {
        if (consuming || access==null) return;
        access.execute((level,pos) -> {
            if (level instanceof ServerLevel) {
                output.setItem(0,valid() ? DyeWorkshop.soak(input.getItem(0),input.getItem(1).get(DataComponents.DYE)) : ItemStack.EMPTY);
                broadcastChanges();
            }
        });
    }
    private void consume(Player player) {
        if (player.level().isClientSide() || !valid()) return;
        consuming=true;
        input.removeItem(0,1);input.removeItem(1,1);
        input.setItem(2,new ItemStack(Items.BUCKET));
        consuming=false;
        slotsChanged(input);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (index<0 || index>=slots.size()) return ItemStack.EMPTY;
        var slot=slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if (index==0) {
            if (!moveItemStackTo(stack,4,40,true)) return ItemStack.EMPTY;
        } else if (index<4) {
            if (!moveItemStackTo(stack,4,40,false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack,1,4,false)) {
            if (index<31) {
                if (!moveItemStackTo(stack,31,40,false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack,4,31,false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount()==original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player,index==0 ? original : stack);
        return original;
    }
    @Override public boolean stillValid(Player player) { return stillValid(access,player,DyeWorkshop.VAT); }
    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level,pos) -> clearContainer(player,input));
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack,Slot slot) {
        return slot.container!=output && super.canTakeItemForPickAll(stack,slot);
    }
}
