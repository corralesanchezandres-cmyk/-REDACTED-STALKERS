package com.terrormod.stalker.client;

import com.terrormod.stalker.StalkerMod;
import com.terrormod.stalker.entity.StalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class StalkerRenderer extends GeoEntityRenderer<StalkerEntity> {
    public StalkerRenderer(EntityRendererProvider.Context context) {
        // true = la cabeza sigue la mirada de la entidad
        super(context, new DefaultedEntityGeoModel<>(new ResourceLocation(StalkerMod.MODID, "stalker"), true));
        this.shadowRadius = 0.0F;
        // Ojos brillantes: usa textures/entity/stalker_glowmask.png
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
