package org.ashmeetop.overdrive_2.client.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;

public class EntityOptimizer {

    public static boolean shouldRenderEntity(Entity entity) {
        if (entity == null) return false;

        // Don't render entities that are too far
        double distance = entity.squaredDistanceTo(MinecraftClient.getInstance().player);
        if (distance > 1024) return false; // 32 blocks squared

        // Optimize item entities on mobile
        if (entity instanceof ItemEntity) {
            return distance < 256; // Only render items within 16 blocks
        }

        // Reduce mob rendering distance
        if (entity instanceof MobEntity) {
            return distance < 2048; // 45 blocks squared
        }

        return true;
    }
}