package studio.detemkur.dyeworkshop;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class DryingRackEntity extends BlockEntity {
    private final NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    private int ticks;
    public DryingRackEntity(BlockPos pos,BlockState state) { super(DyeWorkshop.RACK_ENTITY,pos,state); }
    public ItemStack item() { return items.getFirst(); }
    public void setItem(ItemStack stack) { items.set(0,stack); ticks=0; sync(); }
    public ItemStack take() { var result=item(); setItem(ItemStack.EMPTY); return result; }
    public void tick() {
        if (!DyeWorkshop.wet(item())) return;
        ticks++;
        setChanged();
        if (ticks >= 200) setItem(DyeWorkshop.dry(item()));
    }
    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state) {
        if (level != null && !level.isClientSide()) Block.popResource(level,pos,take());
        super.preRemoveSideEffects(pos,state);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items.set(0,ItemStack.EMPTY); ContainerHelper.loadAllItems(input,items);
        ticks=Math.clamp(input.getIntOr("drying_ticks",0),0,200);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output,items); output.putInt("drying_ticks",ticks);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,registries);
        saveAdditional(output);return output.buildResult();
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
