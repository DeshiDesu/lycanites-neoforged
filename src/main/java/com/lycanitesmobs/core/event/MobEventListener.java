package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
import com.lycanitesmobs.core.event.mobevent.MobEventSchedule;
import com.lycanitesmobs.core.event.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.RandomMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.TickMobEventTrigger;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MobEventListener {
    // Global:
    protected static MobEventListener INSTANCE;

    public List<RandomMobEventTrigger> randomMobEventTriggers = new ArrayList<>();
    public List<TickMobEventTrigger> tickMobEventTriggers = new ArrayList<>();
    private static final UUID swiftswimmingMoveBoostUUID = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5e");
    private static final AttributeModifier swiftswimmingMoveBoost = (new AttributeModifier(swiftswimmingMoveBoostUUID, "Swiftswimming Speed Boost", 1D, AttributeModifier.Operation.ADDITION));
    private static final UUID swiftswimmingMoveBoostUUID2 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5d");
    private static final AttributeModifier swiftswimmingMoveBoost2 = (new AttributeModifier(swiftswimmingMoveBoostUUID2, "Swiftswimming Speed Boost 2", 2D, AttributeModifier.Operation.ADDITION));
    private static final UUID swiftswimmingMoveBoostUUID3 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5c");
    private static final AttributeModifier swiftswimmingMoveBoost3 = (new AttributeModifier(swiftswimmingMoveBoostUUID3, "Swiftswimming Speed Boost 3", 3D, AttributeModifier.Operation.ADDITION));
    private static final UUID swiftswimmingMoveBoostUUID4 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5b");
    private static final AttributeModifier swiftswimmingMoveBoost4 = (new AttributeModifier(swiftswimmingMoveBoostUUID4, "Swiftswimming Speed Boost 4", 4D, AttributeModifier.Operation.ADDITION));


    /**
     * Returns the main Mob Event Listener instance.
     **/
    public static MobEventListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MobEventListener();
        }
        return INSTANCE;
    }


    /**
     * Adds a new Mob Event Trigger.
     *
     * @return True on success, false if it failed to add (could happen if the Trigger type has no matching list created yet).
     */
    public boolean addTrigger(MobEventTrigger mobEventTrigger) {
        if (mobEventTrigger instanceof RandomMobEventTrigger && !this.randomMobEventTriggers.contains(mobEventTrigger)) {
            this.randomMobEventTriggers.add((RandomMobEventTrigger) mobEventTrigger);
            return true;
        }
        if (mobEventTrigger instanceof TickMobEventTrigger && !this.tickMobEventTriggers.contains(mobEventTrigger)) {
            this.tickMobEventTriggers.add((TickMobEventTrigger) mobEventTrigger);
            return true;
        }
        return false;
    }

    /**
     * Removes a Mob Event Trigger.
     */
    public void removeTrigger(MobEventTrigger mobEventTrigger) {
        if (this.randomMobEventTriggers.contains(mobEventTrigger)) {
            this.randomMobEventTriggers.remove(mobEventTrigger);
        }
        if (this.tickMobEventTriggers.contains(mobEventTrigger)) {
            this.tickMobEventTriggers.remove(mobEventTrigger);
        }
        if (mobEventTrigger instanceof AltarMobEventTrigger) {
            AltarMobEventTrigger altarMobEventTrigger = (AltarMobEventTrigger) mobEventTrigger;
            altarMobEventTrigger.onRemove();
        }
    }


    /**
     * Called every tick in a world and counts down to the next event then fires it! The countdown is paused during an event.
     **/
    @SubscribeEvent
    public void onWorldUpdate(TickEvent.LevelTickEvent event) {
        Level world = event.level;
        if (world.isClientSide) {
            return;
        }
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null)
            return;

        // Check If Events Are Completely Disabled:
        if (!MobEventManager.getInstance().mobEventsEnabled || world.getDifficulty() == Difficulty.PEACEFUL) {
            if (worldExt.serverWorldEventPlayer != null)
                worldExt.stopWorldEvent();
            return;
        }

        // Only Tick On World Time Ticks:
        if (worldExt.lastEventScheduleTime == world.getGameTime())
            return;
        worldExt.lastEventScheduleTime = world.getGameTime();

        // Only Run If Players Are Present:
//		if(world.getPlayers().size() < 1) {
//			return;
//		}

        // Scheduled Mob Events:
        for (MobEventSchedule mobEventSchedule : MobEventManager.getInstance().mobEventSchedules) {
            if (mobEventSchedule.canStart(world)) {
                mobEventSchedule.start(worldExt);
            }
        }

        // Tick Mob Events:
        for (TickMobEventTrigger mobEventTrigger : this.tickMobEventTriggers) {
            mobEventTrigger.onTick(world, worldExt.lastEventScheduleTime);
        }

        // Random Mob Events:
        if (MobEventManager.getInstance().mobEventsRandom) {
            if (MobEventManager.getInstance().minEventsRandomDay > 0 && Math.floor((worldExt.useTotalWorldTime ? world.getGameTime() : world.getDayTime()) / 24000D) < MobEventManager.getInstance().minEventsRandomDay) {
                return;
            }
            if (worldExt.getWorldEventStartTargetTime() <= 0 || worldExt.getWorldEventStartTargetTime() > world.getGameTime() + MobEventManager.getInstance().maxTicksUntilEvent) {
                worldExt.setWorldEventStartTargetTime(world.getGameTime() + worldExt.getRandomEventDelay(world.random));
            }
            if (world.getGameTime() == worldExt.getWorldEventStartTargetTime()) {
                this.triggerRandomMobEvent(world, worldExt, 1);
            } else if (world.getGameTime() > worldExt.getWorldEventStartTargetTime()) {
                worldExt.setWorldEventStartTargetTime(0);
            }
        }
    }


    /**
     * Triggers a Random Mob Event Trigger if one is available.
     *
     **/
    public void triggerRandomMobEvent(Level world, ExtendedWorld worldExt, int level) {
        // Get Triggers and Total Weight:
        List<RandomMobEventTrigger> validTriggers = new ArrayList<>();
        int totalWeights = 0;
        int highestPriority = 0;
        for (RandomMobEventTrigger mobEventTrigger : this.randomMobEventTriggers) {
            if (mobEventTrigger.priority >= highestPriority && mobEventTrigger.canTrigger(world, null)) {
                if (mobEventTrigger.priority > highestPriority) {
                    totalWeights = 0;
                    validTriggers.clear();
                }
                totalWeights += mobEventTrigger.weight;
                highestPriority = mobEventTrigger.priority;
                validTriggers.add(mobEventTrigger);
            }
        }
        if (totalWeights <= 0) {
            return;
        }

        // Fire Random Trigger Using Weights:
        int randomWeight = 1;
        if (totalWeights > 1) {
            randomWeight = world.random.nextInt(totalWeights - 1) + 1;
        }
        int searchWeight = 0;
        for (RandomMobEventTrigger mobEventTrigger : validTriggers) {
            if (mobEventTrigger.weight + searchWeight > randomWeight) {
                mobEventTrigger.trigger(world, null, new BlockPos(0, 0, 0), level, -1);
                return;
            }
            searchWeight += mobEventTrigger.weight;
        }
    }

    // ==================================================
    //                   Entity Update
    // ==================================================
    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        // Null Effect Fix:
        for (Object potionEffectObj : entity.getActiveEffects()) {
            if (potionEffectObj == null) {
                entity.removeAllEffects();
                LMHelperClass.logWarning("EffectsSetup", "Found a null potion effect on entity: " + entity + " all effects have been removed from this entity.");
            }
        }

        // Night Vision Stops Blindness:
        if (entity.hasEffect(MobEffects.BLINDNESS) && entity.hasEffect(MobEffects.NIGHT_VISION)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }


        // Disable Nausea:
        EffectManager.getInstance().disableNausea = ConfigExtra.INSTANCE.disableNausea.get();
        if (EffectManager.getInstance().disableNausea && event.getEntity() instanceof Player) {
            if (entity.hasEffect(MobEffects.CONFUSION)) {
                entity.removeEffect(MobEffects.CONFUSION);
            }
        }

        // Immunity:
        boolean invulnerable = false;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            invulnerable = player.isCreative() || player.isSpectator();
        }


        // ========== Debuffs ==========
        // Paralysis
        EffectBase paralysis = ObjectManager.getEffect("paralysis");
        if (paralysis != null) {
            if (!invulnerable && entity.hasEffect(paralysis)) {
                entity.setDeltaMovement(0, entity.getDeltaMovement().y() > 0 ? 0 : entity.getDeltaMovement().y(), 0);
                entity.setOnGround(false);
            }
        }

        // Weight
        EffectBase weight = ObjectManager.getEffect("weight");
        if (weight != null) {
            if (!invulnerable && entity.hasEffect(weight) && !entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
                if (entity.getDeltaMovement().y() > -0.2D)
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.2D, 0));
            }
        }

        // Fear — spawn an EntityFear to haunt the player if one isn't already present.
        EffectBase fear = ObjectManager.getEffect("fear");
        if (fear != null && !entity.getCommandSenderWorld().isClientSide) {
            if (!invulnerable && entity.hasEffect(fear) && entity instanceof Player player) {
                EntityFear.spawnForPlayer(player, null);
            }
        }

        // Instability
        EffectBase instability = ObjectManager.getEffect("instability");
        if (instability != null && !entity.getCommandSenderWorld().isClientSide && !(entity instanceof IGroupBoss)) {
            if (!invulnerable && entity.hasEffect(instability)) {
                if (entity.getCommandSenderWorld().random.nextDouble() <= 0.1) {
                    double strength = 1 + entity.getEffect(instability).getAmplifier();
                    double motionX = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
                    double motionY = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
                    double motionZ = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
                    try {
                        if (entity instanceof ServerPlayer) {
                            ServerPlayer player = (ServerPlayer) entity;
                            player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                            MessageEntityVelocity messageEntityVelocity = new MessageEntityVelocity(
                                    player,
                                    strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
                                    strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
                                    strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D)
                            );
                            LycanitesMobs.PACKET_MANAGER.sendToPlayer(messageEntityVelocity, player);
                        }
                    } catch (Exception e) {
                        LMHelperClass.logWarning("", "Failed to create and send a network packet for instability velocity!");
                        e.printStackTrace();
                    }
                }
            }
        }

        // Plague
        EffectBase plague = ObjectManager.getEffect("plague");
        if (plague != null && !entity.getCommandSenderWorld().isClientSide) {
            if (!invulnerable && entity.hasEffect(plague)) {

                // Poison:
                int poisonAmplifier = entity.getEffect(plague).getAmplifier();
                int poisonDuration = entity.getEffect(plague).getDuration();
                if (entity.hasEffect(MobEffects.POISON)) {
                    poisonAmplifier = Math.max(poisonAmplifier, entity.getEffect(MobEffects.POISON).getAmplifier());
                    poisonDuration = Math.max(poisonDuration, entity.getEffect(MobEffects.POISON).getDuration());
                }
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, poisonAmplifier));

                // Spread:
                if (entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
                    List aoeTargets = EffectManager.getInstance().getNearbyEntities(entity, LivingEntity.class, null, 2);
                    for (Object entityObj : aoeTargets) {
                        LivingEntity target = (LivingEntity) entityObj;
                        if (target != entity && !entity.isAlliedTo(target)) {
                            if (target instanceof Player && !entity.hasLineOfSight(target)) {
                                continue;
                            }
                            int amplifier = entity.getEffect(plague).getAmplifier();
                            int duration = entity.getEffect(plague).getDuration();
                            if (amplifier > 0) {
                                target.addEffect(new MobEffectInstance(plague, duration, amplifier - 1));
                            } else {
                                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
                            }
                        }
                    }
                }
            }
        }

        // Smited
        EffectBase smited = ObjectManager.getEffect("smited");
        if (smited != null && !entity.getCommandSenderWorld().isClientSide) {
            if (!invulnerable && entity.hasEffect(smited) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
                float brightness = LMHelperClass.getBrightness(entity);
                if (brightness > 0.5F && entity.getCommandSenderWorld().canSeeSkyFromBelowWater(entity.blockPosition())) {
                    entity.setSecondsOnFire(4);
                }
            }
        }

        // Bleed
        EffectBase bleed = ObjectManager.getEffect("bleed");
        if (bleed != null && !entity.getCommandSenderWorld().isClientSide) {
            if (!invulnerable && entity.hasEffect(bleed) && entity.getCommandSenderWorld().getGameTime() % 20 == 0 && entity.getVehicle() == null) {
                if (entity.walkDistO != entity.walkDist) {
                    entity.hurt(event.getEntity().level().damageSources().magic(), entity.getEffect(bleed).getAmplifier() + 1);
                }
            }
        }

        // Smouldering
        EffectBase smouldering = ObjectManager.getEffect("smouldering");
        if (smouldering != null && !entity.getCommandSenderWorld().isClientSide) {
            if (!invulnerable && entity.hasEffect(smouldering) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
                entity.setSecondsOnFire(4 + (4 * entity.getEffect(smouldering).getAmplifier()));
            }
        }


        // ========== Buffs ==========
        // Swiftswimming
        EffectBase swiftswimming = ObjectManager.getEffect("swiftswimming");
        if (swiftswimming != null && entity instanceof Player) {
            Player player = (Player) entity;
            AttributeInstance movement = entity.getAttribute(ForgeMod.SWIM_SPEED.get());
            int amplifier = -1;
            if (entity.hasEffect(swiftswimming)) {
                amplifier = entity.getEffect(swiftswimming).getAmplifier();
            }
            if (amplifier == 0 && movement.getModifier(swiftswimmingMoveBoostUUID) == null) {
                movement.addPermanentModifier(swiftswimmingMoveBoost);
            } else if (amplifier != 0 && movement.getModifier(swiftswimmingMoveBoostUUID) != null) {
                movement.removeModifier(swiftswimmingMoveBoost);
            }
            if (amplifier == 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) == null) {
                movement.addPermanentModifier(swiftswimmingMoveBoost2);
            } else if (amplifier != 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) != null) {
                movement.removeModifier(swiftswimmingMoveBoost2);
            }
            if (amplifier == 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) == null) {
                movement.addPermanentModifier(swiftswimmingMoveBoost3);
            } else if (amplifier != 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) != null) {
                movement.removeModifier(swiftswimmingMoveBoost3);
            }
            if (amplifier >= 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) == null) {
                movement.addPermanentModifier(swiftswimmingMoveBoost4);
            } else if (amplifier < 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) != null) {
                movement.removeModifier(swiftswimmingMoveBoost4);
            }
        }

        // Immunisation
        EffectBase immunization = ObjectManager.getEffect("immunization");
        if (immunization != null && !entity.getCommandSenderWorld().isClientSide) {
            if (entity.hasEffect(ObjectManager.getEffect("immunization"))) {
                if (entity.hasEffect(MobEffects.POISON)) {
                    entity.removeEffect(MobEffects.POISON);
                }
                if (entity.hasEffect(MobEffects.HUNGER)) {
                    entity.removeEffect(MobEffects.HUNGER);
                }
                if (entity.hasEffect(MobEffects.WEAKNESS)) {
                    entity.removeEffect(MobEffects.WEAKNESS);
                }
                if (entity.hasEffect(MobEffects.CONFUSION)) {
                    entity.removeEffect(MobEffects.CONFUSION);
                }
                if (ObjectManager.getEffect("paralysis") != null) {
                    if (entity.hasEffect(ObjectManager.getEffect("paralysis"))) {
                        entity.removeEffect(ObjectManager.getEffect("paralysis"));
                    }
                }
            }
        }

        // Cleansed
        EffectBase cleansed = ObjectManager.getEffect("cleansed");
        if (ObjectManager.getEffect("cleansed") != null && !entity.getCommandSenderWorld().isClientSide) {
            if (entity.hasEffect(ObjectManager.getEffect("cleansed"))) {
                if (entity.hasEffect(MobEffects.WITHER)) {
                    entity.removeEffect(MobEffects.WITHER);
                }
                if (entity.hasEffect(MobEffects.UNLUCK)) {
                    entity.removeEffect(MobEffects.UNLUCK);
                }
                if (ObjectManager.getEffect("fear") != null) {
                    if (entity.hasEffect(ObjectManager.getEffect("fear"))) {
                        entity.removeEffect(ObjectManager.getEffect("fear"));
                    }
                }
                if (ObjectManager.getEffect("insomnia") != null) {
                    if (entity.hasEffect(ObjectManager.getEffect("insomnia"))) {
                        entity.removeEffect(ObjectManager.getEffect("insomnia"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide) {
            return;
        }

        Level world = player.getCommandSenderWorld();
        for (EntityFear fearEntity : world.getEntitiesOfClass(EntityFear.class, player.getBoundingBox().inflate(128.0D), fear -> player.equals(fear.hauntTarget))) {
            fearEntity.discard();
        }
    }


    // ==================================================
    //                    Entity Jump
    // ==================================================
    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null)
            return;

        boolean invulnerable = false;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            invulnerable = player.getAbilities().invulnerable;
        }
        if (invulnerable) {
            return;
        }

        // Anti-Jumping:
        EffectBase paralysis = ObjectManager.getEffect("paralysis");
        if (paralysis != null) {
            if (entity.hasEffect(paralysis)) {
                if (event.isCancelable()) event.setCanceled(true);
            }
        }

        EffectBase weight = ObjectManager.getEffect("weight");
        if (weight != null) {
            if (entity.hasEffect(weight)) {
                if (event.isCancelable()) event.setCanceled(true);
            }
        }
    }


    // ==================================================
    //               Living Attack Event
    // ==================================================
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.isCancelable() && event.isCanceled())
            return;

        if (event.getEntity() == null)
            return;

        LivingEntity target = event.getEntity();
        LivingEntity attacker = null;
        if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getSource().getEntity();
        }
        if (attacker == null) {
            return;
        }

        // ========== Debuffs ==========
        // Lifeleak
        EffectBase lifeleak = ObjectManager.getEffect("lifeleak");
        if (lifeleak != null && !event.getEntity().getCommandSenderWorld().isClientSide) {
            if (attacker.hasEffect(lifeleak)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
                target.heal(event.getAmount());
            }
        }
    }


    // ==================================================
    //                 Living Hurt Event
    // ==================================================
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.isCancelable() && event.isCanceled())
            return;

        if (event.getEntity() == null)
            return;
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();


        // ========== Debuffs ==========
        // Fall Resistance
        EffectBase fallresist = ObjectManager.getEffect("fallresist");
        if (fallresist != null) {
            if (event.getEntity().hasEffect(fallresist)) {
                if ("fall".equals(event.getSource().getMsgId())) {
                    event.setAmount(0);
                    event.setCanceled(true);
                }
            }
        }

        // Penetration
        EffectBase penetration = ObjectManager.getEffect("penetration");
        if (penetration != null) {
            if (event.getEntity().hasEffect(penetration)) {
                float damage = event.getAmount();
                float multiplier = 0.25F * (event.getEntity().getEffect(penetration).getAmplifier() + 1);
                event.setAmount(damage + (damage * multiplier));
            }
        }

        // Fear
        EffectBase fear = ObjectManager.getEffect("fear");
        if (fear != null) {
            if (event.getEntity().hasEffect(fear)) {
                if ("inWall".equals(event.getSource().getMsgId())) {
                    event.setAmount(0);
                    event.setCanceled(true);
                }
            }
        }


        // ========== Buffs ==========
        // Leeching
        EffectBase leech = ObjectManager.getEffect("leech");
        if (leech != null && event.getSource().getEntity() != null) {
            LivingEntity leechingEntity = null;
            if (event.getSource().getDirectEntity() instanceof LivingEntity) {
                leechingEntity = (LivingEntity) event.getSource().getDirectEntity();
            } else if (event.getSource().getEntity() instanceof LivingEntity) {
                leechingEntity = (LivingEntity) event.getSource().getEntity();
            }
            if (leechingEntity != null) {
                if (leechingEntity.hasEffect(leech)) {
                    int leeching = leechingEntity.getEffect(leech).getAmplifier() + 1;
                    leechingEntity.heal(Math.max(leeching, 1));
                }
            }
        }

        // Repulsion
        EffectBase repulsion = ObjectManager.getEffect("repulsion");
        if (repulsion != null) {
            boolean attackerIsBoss = attacker instanceof IGroupBoss;
            if (!attackerIsBoss && CreatureManager.getInstance().getCreatureGroup("boss") != null) {
                attackerIsBoss = CreatureManager.getInstance().getCreatureGroup("boss").hasEntity(attacker);
            }
            if (attacker != null && !attackerIsBoss && target.hasEffect(repulsion)) {
                double knockback = target.getEffect(repulsion).getAmplifier() + 2;
                double xDist = attacker.position().x() - target.position().x();
                double zDist = attacker.position().z() - target.position().z();
                double xzDist = Math.max(Mth.sqrt(LMHelperClass.convertToFloat(xDist * xDist + zDist * zDist)), 0.01D);
                double motionCap = 10;
                double xVel = xDist / xzDist * knockback;
                double zVel = zDist / xzDist * knockback;
                if (attacker.getDeltaMovement().x() < motionCap && attacker.getDeltaMovement().x() > -motionCap && attacker.getDeltaMovement().z() < motionCap && attacker.getDeltaMovement().z() > -motionCap) {
                    attacker.push(xVel, 0, zVel);
                }
            }
        }
    }


    // ==================================================
    //                    Entity Heal
    // ==================================================
    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null)
            return;

        // Rejuvenation:
        EffectBase rejuvenation = ObjectManager.getEffect("rejuvenation");
        if (rejuvenation != null) {
            if (entity.hasEffect(rejuvenation)) {
                event.setAmount((float) Math.ceil(event.getAmount() * (2 * (1 + entity.getEffect(rejuvenation).getAmplifier()))));
            }
        }

        // Decay:
        EffectBase decay = ObjectManager.getEffect("decay");
        if (decay != null) {
            if (entity.hasEffect(decay)) {
                event.setAmount((float) Math.floor(event.getAmount() / (2 * (1 + entity.getEffect(decay).getAmplifier()))));
            }
        }
    }


    // ==================================================
    //                Player Use Bed Event
    // ==================================================

    /**
     * This uses the player sleep in bed event to spawn mobs.
     **/
    @SubscribeEvent
    public void onSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide || event.isCanceled())
            return;

        // Insomnia:
        EffectBase insomnia = ObjectManager.getEffect("insomnia");
        if (insomnia != null && player.hasEffect(insomnia)) {
            event.setResult(Player.BedSleepingProblem.NOT_SAFE);
        }
    }


    // ==================================================
    //               Item Use Event
    // ==================================================
    @SubscribeEvent
    public void onLivingUseItem(LivingEntityUseItemEvent event) {
        if (event.isCancelable() && event.isCanceled())
            return;

        if (event.getEntity() == null)
            return;

        // ========== Debuffs ==========
        // Aphagia
        EffectBase aphagia = ObjectManager.getEffect("aphagia");
        if (aphagia != null && !event.getEntity().getCommandSenderWorld().isClientSide) {
            if (event.getEntity().hasEffect(aphagia)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }


    public static void logBiomeAt(Level world, BlockPos pos) {
        ResourceLocation biomeId = world
                .getBiome(pos)
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        LMHelperClass.logInfo(
                "Dungeon",
                "[BiomeDebug] World biome at " + pos + " is " + biomeId
        );
    }


}
