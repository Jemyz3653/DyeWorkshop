package studio.detemkur.dyeworkshop;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

final class WorkshopBlock extends Block implements EntityBlock {
    private final boolean rack;
    WorkshopBlock(boolean rack,Properties properties) { super(properties); this.rack=rack; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return rack ? new DryingRackEntity(pos,state) : null; }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if (!rack || level.isClientSide() || type != DyeWorkshop.RACK_ENTITY) return null;
        return (world,pos,block,entity) -> ((DryingRackEntity)entity).tick();
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,
            Player player,InteractionHand hand,BlockHitResult hit) {
        if (!rack || !DyeWorkshop.armor(stack)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DryingRackEntity entity && entity.item().isEmpty()) {
            entity.setItem(stack.copyWithCount(1));
            stack.consume(1,player);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (rack && level.getBlockEntity(pos) instanceof DryingRackEntity entity) {
                var stack=entity.take();
                if (!stack.isEmpty() && !player.getInventory().add(stack)) Block.popResource(level,pos,stack);
            } else if (!rack) player.openMenu(new SimpleMenuProvider((id,inventory,p) ->
                new SoakingMenu(id,inventory,ContainerLevelAccess.create(level,pos)),Component.translatable("block.dyeworkshop.dye_cauldron")));
        }
        return InteractionResult.SUCCESS;
    }
}
