package base.mixin.client.accessors;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor for FontManager to access font sets by identifier.
 */
@Mixin(FontManager.class)
public interface FontManagerAccessor {

    @Accessor
    FontSet getMissingFontSet();

    @Invoker
    FontSet callGetFontSetRaw(Identifier id);
}
