package base.client.feature.impl.visual;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.*;

import java.util.List;
import java.awt.*;

public class ModuleList extends Module {
    public static ModeSetting sortMode;
    public static ModeSetting fontRenderType;
    public static ModeSetting borderMode;

    public static BooleanSetting backGround;
    public static BooleanSetting backGroundGradient = new BooleanSetting("BackGround Gradient", false, () -> backGround.isEnabled());
    public static ColorSetting backGroundColor2 = new ColorSetting("BackGround Color Two", Color.BLACK.getRGB(), () -> backGround.isEnabled() && backGroundGradient.isEnabled());
    public static ColorSetting backGroundColor = new ColorSetting("BackGround Color", Color.BLACK.getRGB(), () -> backGround.isEnabled());
    public static BooleanSetting border;
    public static BooleanSetting rightBorder;
    public static NumberSetting x;
    public static NumberSetting y;
    public static NumberSetting offset;
    public static NumberSetting size;
    public static NumberSetting borderWidth;
    public static NumberSetting rainbowSaturation;
    public static NumberSetting rainbowBright;
    public static NumberSetting fontX;
    public static NumberSetting fontY;
    public static BooleanSetting blur = new BooleanSetting("Blur", false, () -> backGround.isEnabled());
    public static BooleanSetting suffix;
    public static ModeSetting colorSuffixMode = new ModeSetting("Suffix Mode Color", "Default", () -> suffix.isEnabled(), "Astolfo", "Default", "Static", "Rainbow", "Custom", "Category");
    public static ColorSetting suffixColor = new ColorSetting("Suffix Color", Color.GRAY.getRGB(), () -> colorSuffixMode.currentMode.equals("Custom") || colorSuffixMode.currentMode.equals("Static") && suffix.isEnabled());
    public static ModeSetting position = new ModeSetting("Position", "Right", () -> true, "Right", "Left");

    public ModuleList() {
        super("ModuleList", "Показывает список всех включенных модулей", Type.Visuals);

        /* COLOR SETTINGS */

        /* OTHER */

        borderMode = new ModeSetting("Border Mode", "Full", () -> border.isEnabled(), "Full", "Single");
        sortMode = new ModeSetting("FeatureList Sort", "Length", () -> true, "Length", "Alphabetical");
        fontRenderType = new ModeSetting("FontRender Type", "Shadow", () -> true, "Default", "Shadow", "Outline");
        backGround = new BooleanSetting("Background", true, () -> true);
        border = new BooleanSetting("Border", true, () -> true);
        rightBorder = new BooleanSetting("Right Border", true, () -> true);
        suffix = new BooleanSetting("Suffix", true, () -> true);
        //    alpha = new NumberSetting("BackgroundAlpha", 1, 1, 255, 1, () -> backGround.getCurrentValue() && !blur.getCurrentValue());
        //   bright = new NumberSetting("BackgroundBright", 255, 1, 255, 1, () -> backGround.getCurrentValue() && !blur.getCurrentValue());
        rainbowSaturation = new NumberSetting("Rainbow Saturation", 0.8F, 0.1F, 1F, 0.1F, () -> colorSuffixMode.currentMode.equals("Rainbow"));
        rainbowBright = new NumberSetting("Rainbow Brightness", 1F, 0.1F, 1F, 0.1F, () -> colorSuffixMode.currentMode.equals("Rainbow"));
        fontX = new NumberSetting("FontX", 0, -4, 20, 0.1F, () -> true);
        fontY = new NumberSetting("FontY", 0, -4, 20, 0.01F, () -> true);
        x = new NumberSetting("FeatureList X", 0, 0, 500, 1, () -> !blur.isEnabled());
        y = new NumberSetting("FeatureList Y", 0, 0, 500, 1, () -> !blur.isEnabled());
        size = new NumberSetting("Size", 0.25F, 1, 4F, 0.01F, () -> true);
        offset = new NumberSetting("Font Offset", 11, 7, 20, 0.5F, () -> true);
        borderWidth = new NumberSetting("Border Width", 1, 0, 10, 0.1F, () -> rightBorder.isEnabled());
        addSettings(position, sortMode, fontRenderType, borderMode, colorSuffixMode, suffixColor,  fontX, fontY, border, rightBorder, suffix, borderWidth, backGround, backGroundGradient, backGroundColor, backGroundColor2, rainbowSaturation, rainbowBright, x, y, offset);
    }







  /*  @EventTarget
    public void onRender2D(EventRenderGui event) {
        String arraySort = sortMode.getCurrentMode();
        if (Client.instance.featureManager.getModuleByClass(ModuleList.class).getState() && !mc.options.reducedDebugInfo().get()) {
            Client.instance.featureManager.getModuleList().sort(arraySort.equalsIgnoreCase("Alphabetical") ?
                    Comparator.comparing(Module::getLabel) :
                    Comparator.comparingInt(module -> !HUD.font.currentMode.equals("Minecraft")
                            ? -ClientHelper.getFontRender().getStringWidth(isSuffixEnabled(module)
                            ? module.getSuffix() : module.getLabel()) : -mc.fontRendererObj.getStringWidth(isSuffixEnabled(module)
                            ? module.getSuffix() : module.getLabel())));



            float yPotion = 2;

            for(MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                if(mobeffectinstance.getEffect().value().getCategory().equals(MobEffectCategory.BENEFICIAL)) {
                    yPotion = 26;
                }
                if(mobeffectinstance.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)){
                    yPotion = 26 * 2;
                }

            }




            if(position.getCurrentMode().equals("Right")) {
                float width = event.getResolution().getScaledWidth() - (ModuleList.rightBorder.isEnabled() ? borderWidth.getValue() : 0);
                float y = -1;


                int counter=0;
                for (Module feature : Client.instance.featureManager.getModuleList()) {
                    ScreenHelper animationHelper = feature.getScreenHelper();
                    String featureSuffix = isSuffixEnabled(feature) ? feature.getSuffix() : feature.getLabel();
                    float listOffset = ModuleList.offset.getValue();
                    float length = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(featureSuffix) : mc.fontRendererObj.getStringWidth(featureSuffix);

                    float featureX = width - length;
                    boolean state = feature.getState() && feature.isVisible();

                    if (state) {
                        animationHelper.interpolate(featureX, y, 4F * Minecraft.frameTime / 6);
                    } else {
                        animationHelper.interpolate(width, y, 4F * Minecraft.frameTime / 6);
                    }



                    float translateY = animationHelper.getY() + yPotion;
                    float translateX = animationHelper.getX() - (ModuleList.rightBorder.isEnabled() ? 2.5F : 1.5F) - ModuleList.fontX.getValue();
                    int color = 0;
                    int colorCustom = HUD.onecolor.getColorValue();
                    int colorCustom2 = HUD.twocolor.getColorValue();
                    double time = HUD.time.getValue();
                    String mode = HUD.colorList.getOptions();
                    boolean visible = animationHelper.getX() < width;

                    if (visible) {
                        switch (mode.toLowerCase()) {
                            case "rainbow":
                                color = PaletteHelper.rainbow((int) (y * time), ModuleList.rainbowSaturation.getValue(), ModuleList.rainbowBright.getValue()).getRGB();
                                break;
                            case "astolfo":
                                color = PaletteHelper.astolfo( (int) y * 4).getRGB();
                                break;
                            case "static":
                                color = new Color(colorCustom).getRGB();
                                break;
                            case "custom":
                                color = PaletteHelper.fadeColor(new Color(colorCustom).getRGB(), new Color(colorCustom2).getRGB(), (float) Math.abs(((((System.currentTimeMillis() / time) / time) + y * 6L / 61 * 2) % 2)));

                                break;
                            case "fade":
                                color = PaletteHelper.fadeColor(new Color(colorCustom).getRGB(), new Color(colorCustom).darker().darker().getRGB(), (float) Math.abs(((((System.currentTimeMillis() / time) / time) + y * 6L / 60 * 2) % 2)));
                                break;
                            case "none":
                                color = -1;
                                break;
                            case "category":
                                color = feature.getType().getColor();
                                break;
                        }

                        int colorFuffix = 0;
                        String modeSuffix = ModuleList.colorSuffixMode.getOptions();
                        switch (modeSuffix.toLowerCase()) {
                            case "rainbow":
                                colorFuffix = PaletteHelper.rainbow((int) (y * time), ModuleList.rainbowSaturation.getValue(), ModuleList.rainbowBright.getValue()).getRGB();
                                break;
                            case "astolfo":
                                colorFuffix = PaletteHelper.astolfo(false, (int) y * 4).getRGB();
                                break;
                            case "static":
                                colorFuffix = new Color(suffixColor.getColorValue()).getRGB();
                                break;
                            case "default":
                                colorFuffix = new Color(192, 192, 192).getRGB();
                                break;
                            case "category":
                                colorFuffix = feature.getType().getColor();
                                break;
                        }
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-ModuleList.x.getValue(), ModuleList.y.getValue(), 1);

                        Module nextFeature = null;
                        int index = Client.instance.featureManager.getModuleList().indexOf(feature) + 1;

                        if (Client.instance.featureManager.getModuleList().size() > index) {
                            nextFeature = getNextEnabledFeature(Client.instance.featureManager.getModuleList(), index);
                        }


                        if(ModuleList.border.isEnabled()) {
                            if(borderMode.currentMode.equals("Full")) {

                                RectHelper.drawRect(translateX - 3.5, translateY - 1, translateX - 2, translateY + listOffset - 1, color);

                                if(counter==0) {
                                    String name = isSuffixEnabled(feature) ? nextFeature.getSuffix() : nextFeature.getLabel();
                                    float font = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(name) : mc.fontRendererObj.getStringWidth(name);
                                    RectHelper.drawRect(translateX - 3.5, translateY , translateX - 3.5 +length+(length - font), translateY - 2, color);
                                }

                                if (nextFeature != null) {
                                    String name = isSuffixEnabled(feature) ? nextFeature.getSuffix() : nextFeature.getLabel();
                                    float font = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(name) : mc.fontRendererObj.getStringWidth(name);
                                    float dif = (length - font);

                                    RectHelper.drawRect(translateX - 3.5, translateY + listOffset + 1, translateX - 2 + dif, translateY + listOffset - 1, color);

                                } else {
                                    RectHelper.drawRect(translateX - 3.5, translateY + listOffset + 1, width, translateY + listOffset - 1, color);

                                }

                            }else {
                                RectHelper.drawRect(translateX - 3.5, translateY - 1, translateX - 2, translateY + listOffset - 1, color);
                            }
                        }


                        if (ModuleList.backGround.isEnabled()) {
                            if (!backGroundGradient.isEnabled()) {
                                RectHelper.drawRect(translateX - 2, translateY - 1, width, translateY + listOffset - 1, backGroundColor.getColorValue());
                            } else {
                                RectHelper.drawGradientRect(translateX - 2, translateY - 1, width, translateY + listOffset - 1, backGroundColor.getColorValue(), backGroundColor2.getColorValue());
                            }
                        }
//color=0; 
                        if (!HUD.font.currentMode.equals("Minecraft")) {
                            String modeArrayFont = HUD.font.getOptions();
                            float yOffset = modeArrayFont.equalsIgnoreCase("Verdana") ? 0.5f : modeArrayFont.equalsIgnoreCase("Comfortaa") ? 3 : modeArrayFont.equalsIgnoreCase("CircleRegular") ? 0.5f : modeArrayFont.equalsIgnoreCase("Arial") ? 1.3f : modeArrayFont.equalsIgnoreCase("Kollektif") ? 0.9f : modeArrayFont.equalsIgnoreCase("Product Sans") ? 0.5f : modeArrayFont.equalsIgnoreCase("RaleWay") ? 0.3f : modeArrayFont.equalsIgnoreCase("LucidaConsole") ? 3f : modeArrayFont.equalsIgnoreCase("Lato") ? 1.2f : modeArrayFont.equalsIgnoreCase("Open Sans") ? 0.5f : modeArrayFont.equalsIgnoreCase("SF UI") ? 1.3f : 2f;
                            if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Shadow")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawStringWithShadow(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawStringWithShadow(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            } else if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Default")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawString(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawString(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            } else if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Outline")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawStringWithOutline(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawStringWithOutline(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            }
                        } else if (fontRenderType.currentMode.equals("Shadow")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawStringWithShadow(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawStringWithShadow(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        } else if (fontRenderType.currentMode.equals("Default")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawString(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawString(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        } else if (fontRenderType.currentMode.equals("Outline")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawStringWithOutline(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawStringWithOutline(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        }

                        y += listOffset;

                        if (ModuleList.rightBorder.isEnabled()) {
                            float checkY = border.isEnabled() ? 0 : 0.6F;
                            RectHelper.drawRect(width, translateY - 1 , width + borderWidth.getValue(), translateY + listOffset - checkY+1, color);
                        }

                        GlStateManager.popMatrix();
                        counter++;
                    }

                }
            }else if(position.getCurrentMode().equals("Left")) {
                float width =5 - (ModuleList.rightBorder.isEnabled() ? -borderWidth.getValue() : 2);
                float y = -1;
                int counter=0;
                for (Module feature : Client.instance.featureManager.getModuleList()) {
                    ScreenHelper animationHelper = feature.getScreenHelper();
                    String featureSuffix = isSuffixEnabled(feature) ? feature.getSuffix() : feature.getLabel();
                    float length = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(featureSuffix) : mc.fontRendererObj.getStringWidth(featureSuffix);
                    float listOffset = ModuleList.offset.getValue();
                    float featureX = width - length;
                    boolean state = feature.getState() && feature.isVisible();

                    if (state) {
                        animationHelper.interpolate(featureX, y, 4F * Minecraft.frameTime / 6);
                    } else {
                        animationHelper.interpolate(width, y, 4F * Minecraft.frameTime / 6);
                    }

                    //   float yPotion = 2;   for (PotionEffect potionEffect : mc.player.getActivePotionEffects()) {  if (potionEffect.getPotion().isBeneficial()) {       yPotion = 26;       }         if (potionEffect.getPotion().isBadEffect()) {      yPotion = 26 * 2;   }  }

                    float translateY = animationHelper.getY() + 2;
                    float translateX = animationHelper.getX() - (ModuleList.rightBorder.isEnabled() ? 2.5F : 1.5F) - ModuleList.fontX.getValue()+ length;
                    int color = 0;
                    int colorCustom = HUD.onecolor.getColorValue();
                    int colorCustom2 = HUD.twocolor.getColorValue();
                    double time = HUD.time.getValue();
                    String mode = HUD.colorList.getOptions();
                    boolean visible = animationHelper.getX() < width;

                    if (visible) {
                        switch (mode.toLowerCase()) {
                            case "rainbow":
                                color = PaletteHelper.rainbow((int) (y * time), ModuleList.rainbowSaturation.getValue(), ModuleList.rainbowBright.getValue()).getRGB();
                                break;
                            case "astolfo":
                                color = PaletteHelper.astolfo(false, (int) y * 4).getRGB();
                                break;
                            case "static":
                                color = new Color(colorCustom).getRGB();
                                break;
                            case "custom":
                                color = PaletteHelper.fadeColor(new Color(colorCustom).getRGB(), new Color(colorCustom2).getRGB(), (float) Math.abs(((((System.currentTimeMillis() / time) / time) + y * 6L / 61 * 2) % 2)));
                                break;
                            case "fade":
                                color = PaletteHelper.fadeColor(new Color(colorCustom).getRGB(), new Color(colorCustom).darker().darker().getRGB(), (float) Math.abs(((((System.currentTimeMillis() / time) / time) + y * 6L / 60 * 2) % 2)));
                                break;
                            case "none":
                                color = -1;
                                break;
                            case "category":
                                color = feature.getType().getColor();
                                break;
                        }

                        int colorFuffix = 0;
                        String modeSuffix = ModuleList.colorSuffixMode.getOptions();
                        switch (modeSuffix.toLowerCase()) {
                            case "rainbow":
                                colorFuffix = PaletteHelper.rainbow((int) (y * time), ModuleList.rainbowSaturation.getValue(), ModuleList.rainbowBright.getValue()).getRGB();
                                break;
                            case "astolfo":
                                colorFuffix = PaletteHelper.astolfo(false, (int) y * 4).getRGB();
                                break;
                            case "static":
                                colorFuffix = new Color(suffixColor.getColorValue()).getRGB();
                                break;
                            case "default":
                                colorFuffix = new Color(192, 192, 192).getRGB();
                                break;
                            case "category":
                                colorFuffix = feature.getType().getColor();
                                break;
                        }
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-ModuleList.x.getValue(), ModuleList.y.getValue(), 1);

                        Module nextFeature = null;
                        int index = Client.instance.featureManager.getModuleList().indexOf(feature) + 1;

                        if (Client.instance.featureManager.getModuleList().size() > index) {
                            nextFeature = getNextEnabledFeature(Client.instance.featureManager.getModuleList(), index);
                        }

                        if(border.isEnabled()) {
                            if(borderMode.currentMode.equals("Full")) {


                                if(counter==0) {
                                    String name = isSuffixEnabled(feature) ? nextFeature.getSuffix() : nextFeature.getLabel();
                                    float font = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(name) : mc.fontRendererObj.getStringWidth(name);
                                    RectHelper.drawRect(translateX - 3.5, -translateY , width+ length+1.5, translateY, color);
                                }
                                if (nextFeature != null) {//System.out.println("1");
                                    String name = isSuffixEnabled(feature) ? nextFeature.getSuffix() : nextFeature.getLabel();
                                    float font = !HUD.font.currentMode.equals("Minecraft") ? ClientHelper.getFontRender().getStringWidth(name) : mc.fontRendererObj.getStringWidth(name);
                                    float dif = font-length ;
                                    RectHelper.drawRect(width+font, translateY + listOffset + 1, width+ length, translateY + listOffset - 1, color);

                                } else {
                                    RectHelper.drawRect(translateX - 3.5, translateY + listOffset + 1, width+ length, translateY + listOffset - 1, color);
                                }

                            }else {
                                RectHelper.drawRect(width+length, translateY-1 , width+length + 1.5, translateY + listOffset-1, color);
                            }
                            RectHelper.drawRect(width+length, translateY , width+length + 1.5, translateY + listOffset+1, color);
                        }
                        if (ModuleList.backGround.isEnabled()) {
                            if (!backGroundGradient.isEnabled()) {
                                RectHelper.drawRect(translateX - 2, translateY - 1, width+ length, translateY + listOffset - 1, backGroundColor.getColorValue());
                            } else {
                                RectHelper.drawGradientRect(translateX - 2, translateY - 1, width+ length, translateY + listOffset - 1, backGroundColor.getColorValue(), backGroundColor2.getColorValue());
                            }
                        }
                        //color=0; 
                        if (!HUD.font.currentMode.equals("Minecraft")) {
                            String modeArrayFont = HUD.font.getOptions();
                            float yOffset = modeArrayFont.equalsIgnoreCase("Verdana") ? 0.5f : modeArrayFont.equalsIgnoreCase("Comfortaa") ? 3 : modeArrayFont.equalsIgnoreCase("CircleRegular") ? 0.5f : modeArrayFont.equalsIgnoreCase("Arial") ? 1.3f : modeArrayFont.equalsIgnoreCase("Kollektif") ? 0.9f : modeArrayFont.equalsIgnoreCase("Product Sans") ? 0.5f : modeArrayFont.equalsIgnoreCase("RaleWay") ? 0.3f : modeArrayFont.equalsIgnoreCase("LucidaConsole") ? 3f : modeArrayFont.equalsIgnoreCase("Lato") ? 1.2f : modeArrayFont.equalsIgnoreCase("Open Sans") ? 0.5f : modeArrayFont.equalsIgnoreCase("SF UI") ? 1.3f : 2f;
                            if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Shadow")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawStringWithShadow(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawStringWithShadow(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            } else if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Default")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawString(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawString(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            } else if (!HUD.font.currentMode.equals("Minecraft") && fontRenderType.currentMode.equals("Outline")) {
                                if (isSuffixEnabled(feature)) {
                                    ClientHelper.getFontRender().drawStringWithOutline(feature.getSuffix(), translateX, translateY + yOffset + fontY.getValue(), colorFuffix);
                                }
                                ClientHelper.getFontRender().drawStringWithOutline(feature.getLabel(), translateX, translateY + yOffset + fontY.getValue(), color);
                            }
                        } else if (fontRenderType.currentMode.equals("Shadow")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawStringWithShadow(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawStringWithShadow(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        } else if (fontRenderType.currentMode.equals("Default")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawString(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawString(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        } else if (fontRenderType.currentMode.equals("Outline")) {
                            if (isSuffixEnabled(feature)) {
                                mc.fontRendererObj.drawStringWithOutline(feature.getSuffix(), translateX, translateY + 1 + fontY.getValue(), colorFuffix);
                            }
                            mc.fontRendererObj.drawStringWithOutline(feature.getLabel(), translateX, translateY + 1 + fontY.getValue(), color);
                        }

                        y += listOffset;

                        if (ModuleList.rightBorder.isEnabled()) {
                            float checkY = border.isEnabled() ? 0 : 0.6F;
                            RectHelper.drawRect(width- borderWidth.getValue()-5, translateY-1, width -4, translateY + listOffset-1, color);
                        }

                        GlStateManager.popMatrix();
                        counter++;
                    }

                }
            }










        }
    }

*/



    private static Module getNextEnabledFeature(List<Module> features, int index) {
        for (int i = index; i < features.size(); i++) {
            Module feature = features.get(i);
            if (feature.getState() && feature.isVisible()) {
                if (!feature.getSuffix().equals("ClickGui") && feature.isVisible()) {
                    return feature;
                }
            }
        }
        return null;
    }

    boolean isSuffixEnabled(Module m) {
        return suffix.isEnabled() && m.isSuffixVisible();
    }
}
