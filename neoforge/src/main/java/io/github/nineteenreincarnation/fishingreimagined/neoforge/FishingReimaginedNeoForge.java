package io.github.nineteenreincarnation.fishingreimagined.neoforge;

import io.github.nineteenreincarnation.fishingreimagined.FishingReimagined;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingInput;
import io.github.nineteenreincarnation.fishingreimagined.fight.ServerFishingMode;
import io.github.nineteenreincarnation.fishingreimagined.network.FishingModePayload;
import io.github.nineteenreincarnation.fishingreimagined.network.ReelInputPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(FishingReimagined.MOD_ID)
public final class FishingReimaginedNeoForge {
    public FishingReimaginedNeoForge(
        IEventBus modEventBus
    ) {
        FishingReimagined.initialize();

        modEventBus.addListener(
            this::registerPayloads
        );
    }

    private void registerPayloads(
        RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar =
            event.registrar(
                FishingReimagined.MOD_ID
            ).optional();

        registrar.playToServer(
            ReelInputPayload.TYPE,
            ReelInputPayload.STREAM_CODEC,
            (payload, context) ->
                context.enqueueWork(() -> {
                    if (
                        context.flow()
                            == PacketFlow.SERVERBOUND
                            && context.player()
                                instanceof ServerPlayer player
                    ) {
                        ServerFishingInput.update(
                            player,
                            payload.action()
                        );
                    }
                })
        );

        registrar.playToServer(
            FishingModePayload.TYPE,
            FishingModePayload.STREAM_CODEC,
            (payload, context) ->
                context.enqueueWork(() -> {
                    if (
                        context.flow()
                            == PacketFlow.SERVERBOUND
                            && context.player()
                                instanceof ServerPlayer player
                    ) {
                        ServerFishingMode.update(
                            player,
                            payload.enabled()
                        );
                    }
                })
        );
    }
}
