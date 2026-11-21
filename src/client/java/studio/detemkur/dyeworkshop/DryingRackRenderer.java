package studio.detemkur.dyeworkshop;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

final class DryingRackRenderer implements BlockEntityRenderer<DryingRackEntity,DryingRackRenderer.State> {
    private final BlockEntityRendererProvider.Context context;
    DryingRackRenderer(BlockEntityRendererProvider.Context context) { this.context=context; }
    static final class State extends BlockEntityRenderState { final ItemStackRenderState item=new ItemStackRenderState(); }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(DryingRackEntity rack,State state,float delta,Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumble) {
        BlockEntityRenderer.super.extractRenderState(rack,state,delta,camera,crumble);
        state.item.clear();
        if (!rack.item().isEmpty()) context.itemModelResolver().updateForTopItem(
            state.item,rack.item(),ItemDisplayContext.FIXED,rack.getLevel(),null,0);
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector nodes,CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        pose.pushPose();pose.translate(.5,.65,.47);pose.scale(.65F,.65F,.65F);
        state.item.submit(pose,nodes,state.lightCoords,OverlayTexture.NO_OVERLAY,0);pose.popPose();
    }
}
