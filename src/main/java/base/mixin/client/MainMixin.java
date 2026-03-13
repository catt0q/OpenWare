package base.mixin.client;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Main.class)
public abstract class MainMixin {
   /* @Redirect(
            method = "main([Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljoptsimple/ArgumentAcceptingOptionSpec;defaultsTo(Ljava/lang/Object;[Ljava/lang/Object;)Ljoptsimple/ArgumentAcceptingOptionSpec;"
            )
    )
    private static <V> ArgumentAcceptingOptionSpec<V> redirectDefaultsTo(
            ArgumentAcceptingOptionSpec<V> spec, V value, V[] values
    ) {
        if (value instanceof String && ((String) value).startsWith("Player")) {
        //return spec.defaultsTo((V) "Skjwefk");
            return spec.defaultsTo((V) "eury");
        }
        return spec.defaultsTo(value, values);
    }*/
}