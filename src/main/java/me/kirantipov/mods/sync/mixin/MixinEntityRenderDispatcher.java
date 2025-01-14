package me.kirantipov.mods.sync.mixin;

import com.google.common.collect.ImmutableMap;
import me.kirantipov.mods.sync.client.render.entity.ShellEntityRenderer;
import me.kirantipov.mods.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Final
    @Shadow
    private ItemRenderer itemRenderer;

    @Final
    @Shadow
    private TextRenderer textRenderer;

    @Final
    @Shadow
    private EntityModelLoader modelLoader;

    @Unique
    private Map<String, EntityRenderer<? extends PlayerEntity>> shellRenderers = ImmutableMap.of();

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void getRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (entity instanceof ShellEntity shell) {
            EntityRenderer<? extends PlayerEntity> renderer = this.shellRenderers.get(shell.getModel());
            if (renderer != null) {
                cir.setReturnValue((EntityRenderer<? super T>)renderer);
            }
        }
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void reload(ResourceManager manager, CallbackInfo ci) {
        EntityRendererFactory.Context context = new EntityRendererFactory.Context((EntityRenderDispatcher)(Object)this, this.itemRenderer, manager, this.modelLoader, this.textRenderer);
        this.shellRenderers = ImmutableMap.of(
            "default", new ShellEntityRenderer(context, false),
            "slim", new ShellEntityRenderer(context, true)
        );
    }
}