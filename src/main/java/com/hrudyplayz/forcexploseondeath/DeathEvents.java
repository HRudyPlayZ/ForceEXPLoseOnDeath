package com.hrudyplayz.forcexploseondeath;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;


public class DeathEvents {
    public static Logger log = LogManager.getLogger(Main.MODID); // Creates the debug log function.
    private int xpAmount; // Creates the xpAmount value used by both events.

    public DeathEvents() { this.xpAmount = 0; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterPlayerDeath(LivingDeathEvent event) {
    // This event gets triggered just after the player dies. (Useful to spawn the exp orbs)

        // Only does something with keepInventory enabled.
        if (event.entity.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {

            // This ensures the event only gets triggered for valid player deaths.
            if (event.isCanceled() || !(event.entity instanceof EntityPlayer) || (event.entity.worldObj.isRemote)) return;

            try {
                // Grabs the concerned entity (the player).
                EntityLivingBase entity = (EntityLivingBase) event.entity;

                // Spawns the exp orbs like vanilla MC does.
                while (this.xpAmount > 0) {
                    int j = EntityXPOrb.getXPSplit(xpAmount);
                    this.xpAmount -= j;
                    entity.worldObj.spawnEntityInWorld(new EntityXPOrb(entity.worldObj, entity.posX, entity.posY, entity.posZ, j));
                }

            } catch (Exception e) { // Just in case the death processing somehow fails, we never know.
                log.log(Level.WARN, "Error processing the death of '" + event.entity.getCommandSenderName() + "'");
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterPlayerRespawn(PlayerEvent.Clone event) {
        // This event gets triggered after the player clicks the respawn button (useful to clear the exp bar).

        // Only does something with keepInventory enabled.
        if (event.entityPlayer.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            // This ensures only the death event can trigger the method, as players also get cloned when switching dimensions.
            if (event.isCanceled() || !event.wasDeath) return;

            // Finds the right XP amount that will need to be dropped by the afterPlayerDeath event.
            xpAmount = Math.min(event.entityPlayer.experienceLevel * 7, 100);

            // Resets the player experience count to 0.
            event.entityPlayer.experienceLevel = 0;
            event.entityPlayer.experience = 0.0F;
            event.entityPlayer.experienceTotal = 0;
        }
    }
}
