package studio.detemkur.dyeworkshop;

import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class DyeWorkshop implements ModInitializer {
    public static Block VAT, RACK;
    public static BlockEntityType<DryingRackEntity> RACK_ENTITY;
    public static MenuType<SoakingMenu> MENU;
    public static final String PENDING = "dyeworkshop:pending_color";
    private static final Set<String> MATERIALS = Set.of("leather","copper","chainmail","iron","gold","diamond","netherite","turtle_scute");
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("dyeworkshop",path); }
    @Override public void onInitialize() {
        VAT = register("dye_cauldron",false);
        RACK = register("drying_rack",true);
        RACK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,id("drying_rack"),
            FabricBlockEntityTypeBuilder.create(DryingRackEntity::new,RACK).build());
        MENU = Registry.register(BuiltInRegistries.MENU,id("soaking"),new MenuType<>(SoakingMenu::new,FeatureFlags.VANILLA_SET));
    }
    private static Block register(String path, boolean rack) {
        var key=ResourceKey.create(Registries.BLOCK,id(path));
        var block=Registry.register(BuiltInRegistries.BLOCK,key,new WorkshopBlock(rack,
            BlockBehaviour.Properties.of().setId(key).strength(1.5F).sound(rack ? SoundType.WOOD : SoundType.METAL).noOcclusion()));
        var itemKey=ResourceKey.create(Registries.ITEM,id(path));
        var item=Registry.register(BuiltInRegistries.ITEM,itemKey,new BlockItem(block,new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("minecraft","functional_blocks"))).register(entries -> entries.accept(item));
        return block;
    }
    public static boolean armor(ItemStack stack) {
        var equipment=stack.get(DataComponents.EQUIPPABLE);
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft")
            && equipment != null && equipment.assetId().isPresent()
            && MATERIALS.contains(equipment.assetId().get().identifier().getPath())
            && Set.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET).contains(equipment.slot());
    }
    public static boolean wet(ItemStack stack) {
        var data=stack.get(DataComponents.CUSTOM_DATA);
        return armor(stack) && data != null && data.copyTag().getIntOr(PENDING,-1) >= 0;
    }
    public static ItemStack soak(ItemStack armor, DyeColor color) {
        var result=armor.copyWithCount(1);
        var data=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        data.putInt(PENDING,color.getTextureDiffuseColor() & 0xffffff);
        result.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        return result;
    }
    public static ItemStack dry(ItemStack stack) {
        if (!wet(stack)) return stack;
        var result=stack.copy();
        var data=result.get(DataComponents.CUSTOM_DATA).copyTag();
        int color=data.getIntOr(PENDING,0xffffff) & 0xffffff;
        data.remove(PENDING);
        if (data.isEmpty()) result.remove(DataComponents.CUSTOM_DATA);
        else result.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(color));
        var previous=result.get(DataComponents.EQUIPPABLE);
        var asset=ResourceKey.create(EquipmentAssets.ROOT_ID,id(previous.assetId().orElseThrow().identifier().getPath()));
        result.set(DataComponents.EQUIPPABLE,new Equippable(previous.slot(),previous.equipSound(),Optional.of(asset),
            previous.cameraOverlay(),previous.allowedEntities(),previous.dispensable(),previous.swappable(),
            previous.damageOnHurt(),previous.equipOnInteract(),previous.canBeSheared(),previous.shearingSound()));
        result.set(DataComponents.ITEM_MODEL,id(BuiltInRegistries.ITEM.getKey(result.getItem()).getPath()));
        return result;
    }
}
