package com.simibubi.create.content.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ExtendoGripInteractionPacket extends SimplePacketBase {

	private Hand interactionHand;
	private int target;
	private Vec3d specificPoint;

	public ExtendoGripInteractionPacket(Entity target) {
		this(target, null);
	}

	public ExtendoGripInteractionPacket(Entity target, Hand hand) {
		this(target, hand, null);
	}

	public ExtendoGripInteractionPacket(Entity target, Hand hand, Vec3d specificPoint) {
		interactionHand = hand;
		this.specificPoint = specificPoint;
		this.target = target.getEntityId();
	}

	public ExtendoGripInteractionPacket(PacketBuffer buffer) {
		target = buffer.readInt();
		int handId = buffer.readInt();
		interactionHand = handId == -1 ? null : Hand.values()[handId];
		if (buffer.readBoolean())
			specificPoint = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(target);
		buffer.writeInt(interactionHand == null ? -1 : interactionHand.ordinal());
		buffer.writeBoolean(specificPoint != null);
		if (specificPoint != null) {
			buffer.writeDouble(specificPoint.x);
			buffer.writeDouble(specificPoint.y);
			buffer.writeDouble(specificPoint.z);
		}
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null)
					return;
				Entity entityByID = sender.getServerWorld()
					.getEntityByID(target);
				if (entityByID != null && ExtendoGripItem.isHoldingExtendoGrip(sender)) {
					if (interactionHand == null)
						sender.attackTargetEntityWithCurrentItem(entityByID);
					else if (specificPoint == null)
						sender.interactOn(entityByID, interactionHand);
					else
						entityByID.applyPlayerInteraction(sender, specificPoint, interactionHand);
				}
			});
		context.get()
			.setPacketHandled(true);
	}

}
