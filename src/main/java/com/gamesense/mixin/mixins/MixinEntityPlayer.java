package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.PlayerJumpEvent;
import com.gamesense.api.event.events.TravelEvent;
import com.gamesense.api.event.events.WaterPushEvent;
import com.gamesense.client.GameSense;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @Shadow
    public abstract String getName();

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void onJump(CallbackInfo callbackInfo) {
        if (Minecraft.getMinecraft().player.getName().equals(this.getName())) {
            GameSense.EVENT_BUS.post(new PlayerJumpEvent());
        }
    }

    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    private void onPushedByWater(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        WaterPushEvent event = new WaterPushEvent();
        GameSense.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo callbackInfo) {
    	TravelEvent event = new TravelEvent();
    	GameSense.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
            //move(MoverType.SELF, motionX, motionY, motionZ);
    		callbackInfo.cancel();
    	}
    }
}
