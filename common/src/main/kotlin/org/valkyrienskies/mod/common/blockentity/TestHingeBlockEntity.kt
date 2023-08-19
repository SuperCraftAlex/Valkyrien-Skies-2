package org.valkyrienskies.mod.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.shipObjectWorld

class TestHingeBlockEntity(blockPos: BlockPos, blockState: BlockState) : BlockEntity(
    ValkyrienSkiesMod.TEST_HINGE_BLOCK_ENTITY_TYPE, blockPos, blockState
) {
    var otherHingePos: BlockPos? = null
    var orientationConstraintId: VSConstraintId? = null
    var attachmentConstraintId: VSConstraintId? = null
    var orientation: Quaterniond? = null
    var otherShipId: ShipId? = null
    var mainShipId: ShipId? = null
    var attachmentLocalPos0: Vector3dc? = null
    var attachmentLocalPos1: Vector3dc? = null


    fun tick() {

    }

    override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag)
        return tag
    }

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun saveAdditional(tag: CompoundTag) {
        if(otherHingePos == null)
            return

        tag.putInt("otherX", otherHingePos!!.x)
        tag.putInt("otherY", otherHingePos!!.y)
        tag.putInt("otherZ", otherHingePos!!.z)

        tag.putDouble("orientationX", orientation!!.x)
        tag.putDouble("orientationY", orientation!!.y)
        tag.putDouble("orientationZ", orientation!!.z)
        tag.putDouble("orientationW", orientation!!.w)

        tag.putLong("otherShipId", otherShipId!!)
        tag.putLong("mainShipId", mainShipId!!)

        tag.putDouble("attachmentLocalPos0X", attachmentLocalPos0!!.x())
        tag.putDouble("attachmentLocalPos0Y", attachmentLocalPos0!!.y())
        tag.putDouble("attachmentLocalPos0Z", attachmentLocalPos0!!.z())

        tag.putDouble("attachmentLocalPos1X", attachmentLocalPos1!!.x())
        tag.putDouble("attachmentLocalPos1Y", attachmentLocalPos1!!.y())
        tag.putDouble("attachmentLocalPos1Z", attachmentLocalPos1!!.z())
    }

    override fun load(tag: CompoundTag) {
        otherHingePos = BlockPos(
            tag.getInt("otherX"),
            tag.getInt("otherY"),
            tag.getInt("otherZ"),
        )

        orientation = Quaterniond(
            tag.getDouble("orientationX"),
            tag.getDouble("orientationY"),
            tag.getDouble("orientationZ"),
            tag.getDouble("orientationW"),
        )

        attachmentLocalPos0 = Vector3d(
            tag.getDouble("attachmentLocalPos0X"),
            tag.getDouble("attachmentLocalPos0Y"),
            tag.getDouble("attachmentLocalPos0Z"),
        )

        attachmentLocalPos1 = Vector3d(
            tag.getDouble("attachmentLocalPos1X"),
            tag.getDouble("attachmentLocalPos1Y"),
            tag.getDouble("attachmentLocalPos1Z"),
        )

        otherShipId = tag.getLong("otherShipId")
        mainShipId = tag.getLong("mainShipId")

        if (level == null || level!!.isClientSide) {
            return
        }

        VSEvents.shipLoadEvent.on { (otherShip), handler ->
            if (otherShip.id == otherShipId || otherShip.id == mainShipId) {
                handler.unregister()
                VSEvents.shipLoadEvent.on { (ship), handler2 ->
                    if ((ship.id == mainShipId || ship.id == otherShipId) && ship.id != otherShip.id) {
                        handler2.unregister()

                        run {
                            val attachmentConstraint = VSAttachmentConstraint(
                                otherShipId!!, mainShipId!!, 1e-10, attachmentLocalPos0!!, attachmentLocalPos1!!,
                                1e10, 0.0
                            )
                            attachmentConstraintId = (level as ServerLevel).shipObjectWorld.createNewConstraint(attachmentConstraint)
                        }

                        run {
                            // I don't recommend setting compliance lower than 1e-10 because it tends to cause instability
                            val hingeConstraint = VSHingeOrientationConstraint(
                                otherShipId!!, mainShipId!!, 1e-10, orientation!!, orientation!!, 1e10
                            )
                            orientationConstraintId = (level as ServerLevel).shipObjectWorld.createNewConstraint(hingeConstraint)
                        }

                    }
                }
            }
        }
    }
}
