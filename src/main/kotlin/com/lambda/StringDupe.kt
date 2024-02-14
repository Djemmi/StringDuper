package com.lambda

import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.init.Items.STRING
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

internal object StringDupe : PluginModule(
        name = "StringDupe",
        description = "Dupes strings automatically",
        category = Category.MISC,
        modulePriority = 300,
        pluginMain = ExamplePlugin
) {

    // SETTINGS
    private val delay by setting("Delay", 5, 1..100, 1)
    private val unbreak_detect by setting("DesyncDetect", 60, 20..100, 10)


    // VALUES
    private var TicksPassed = 0
    private var TicksWithString = 0;


    // MAIN LOGIC
    init {

        onEnable {
            TicksPassed = 0
            TicksWithString = 0
        }

        safeListener<ClientTickEvent> {
            ++TicksPassed

            if(mc.objectMouseOver.typeOfHit == Type.ENTITY || mc.objectMouseOver.typeOfHit == Type.MISS) {
                TicksPassed = 0
                return@safeListener
            }

            val mouseBlockState = mc.world.getBlockState(mc.objectMouseOver.blockPos)

            if(mouseBlockState.block == Blocks.TRIPWIRE) {
                ++TicksWithString

            } else {
                TicksWithString = 0
            }

            if(TicksWithString >= unbreak_detect) {
                mc.connection?.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, mc.objectMouseOver.blockPos, EnumFacing.getDirectionFromEntityLiving(mc.objectMouseOver.blockPos, mc.player)))
                TicksWithString = 0
                TicksPassed = 0
                return@safeListener
            }

            if(TicksPassed >= delay) {



                if(mouseBlockState.block == Blocks.AIR) {
                    TicksPassed = 0
                    return@safeListener
                }

                if(mc.objectMouseOver.blockPos.getDistance(mc.player.posX.toInt(), mc.player.posY.toInt(), mc.player.posZ.toInt()) > 4 ) {
                    TicksPassed = 0
                    return@safeListener
                }

                if(mouseBlockState.isNormalCube && mouseBlockState.isFullBlock) {
                    if(mc.player.getHeldItem(MAIN_HAND).item == STRING) {
                        mc.connection?.sendPacket(CPacketPlayerTryUseItemOnBlock(mc.objectMouseOver.blockPos, EnumFacing.getDirectionFromEntityLiving(mc.objectMouseOver.blockPos, mc.player), MAIN_HAND, 0.5f, 0.5f, 0.5f))
                        TicksPassed = 0
                    } else {
                        MessageSendHelper.sendChatMessage("Have no string in main hand. disabling")
                        disable()
                    }
                } else {
                }
            }
        }
    }
}