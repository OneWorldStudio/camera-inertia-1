package starlight_lnk.camerainertia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.phys.Vec3;
import starlight_lnk.camerainertia.config.ClientConfig;

/**
 * рџљЂ Р­С„С„РµРєС‚ СЃРєРѕСЂРѕСЃС‚Рё РїСЂРё РµР·РґРµ РЅР° С‚СЂР°РЅСЃРїРѕСЂС‚Рµ.
 *
 * РЎС‡РёС‚Р°РµС‚ РїР»Р°РІРЅС‹Р№ РЅРѕСЂРјР°Р»РёР·РѕРІР°РЅРЅС‹Р№ С„Р°РєС‚РѕСЂ СЃРєРѕСЂРѕСЃС‚Рё (0..1) СЃ СѓС‡С‘С‚РѕРј С‚РёРїР° РўРЎ.
 * Р­С‚РѕС‚ С„Р°РєС‚РѕСЂ РїРѕС‚СЂРµР±Р»СЏСЋС‚:
 *   - CameraFovController (С‡РµСЂРµР· getFovBoost / getFactorRaw)
 *     в†’ Р»С‘РіРєРѕРµ РѕС‚РґР°Р»РµРЅРёРµ FOV РЅР° СЂР°Р·РіРѕРЅРµ
 *   - CameraTurnBlur      (С‡РµСЂРµР· getBlurIntensity)
 *     в†’ СЃРёРјРјРµС‚СЂРёС‡РЅС‹Р№ side-blur РЅР° РІС‹СЃРѕРєРѕР№ СЃРєРѕСЂРѕСЃС‚Рё
 *
 * Р”РІСѓС…СЃС‚СѓРїРµРЅС‡Р°С‚РѕРµ СЃРіР»Р°Р¶РёРІР°РЅРёРµ:
 *   1) rawSpeed в†’ smoothedSpeed вЂ” С„РёР»СЊС‚СЂ РѕС‚ С‚РёРєРѕРІС‹С… СЂС‹РІРєРѕРІ С„РёР·РёРєРё;
 *   2) target в†’ factor          вЂ” РѕСЃРЅРѕРІРЅРѕР№ С„РёР»СЊС‚СЂ РїР»Р°РІРЅРѕСЃС‚Рё РґР»СЏ FX (РјРµРґР»РµРЅРЅС‹Р№).
 *
 * РџРѕРґРґРµСЂР¶РёРІР°СЋС‚СЃСЏ: Р»РѕРґРєРё, Р»РѕС€Р°РґРё, РІРµСЂР±Р»СЋРґС‹, РІР°РіРѕРЅРµС‚РєРё + fallback РґР»СЏ РјРѕРґРґРѕРІС‹С… РўРЎ.
 * РЎРІРёРЅСЊСЏ/РЎС‚СЂРµР№РґРµСЂ СЃРѕР·РЅР°С‚РµР»СЊРЅРѕ РёСЃРєР»СЋС‡РµРЅС‹ (СЃР»РёС€РєРѕРј РјРµРґР»РµРЅРЅС‹Рµ).
 *
 * рџЋҐ Р Р°Р±РѕС‚Р°РµС‚ С‚РѕР»СЊРєРѕ РІ 1st person:
 *   - РІ 3rd person tick() РІС‹РїРѕР»РЅСЏРµС‚ decay() (РјСЏРіРєРѕРµ Р·Р°С‚СѓС…Р°РЅРёРµ),
 *   - РІСЃРµ РіРµС‚С‚РµСЂС‹ РІРѕР·РІСЂР°С‰Р°СЋС‚ 0, С‡С‚РѕР±С‹ FOV Рё Р±Р»СЋСЂ РІ 3rd person РЅРµ РїРѕРґРјРµС€РёРІР°Р»РёСЃСЊ.
 */
public final class CameraVehicleSpeedFx {

    // ============================================================
    //                       РќРђРЎРўР РћР™РљР
    // ============================================================

    /** Р“Р»РѕР±Р°Р»СЊРЅС‹Р№ switch. */
    private static final boolean ENABLED = true;

    /** Р’РєР»СЋС‡С‘РЅ Р»Рё FOV-РєР°РЅР°Р». */
    private static final boolean FOV_ENABLED = true;
    /** РњР°РєСЃРёРјР°Р»СЊРЅС‹Р№ РІРєР»Р°Рґ РІ FOV-РјРЅРѕР¶РёС‚РµР»СЊ РЅР° РїРёРєРµ (0.12 = +12% FOV). */
    private static final float FOV_STRENGTH = 0.12F;

    /** Р’РєР»СЋС‡С‘РЅ Р»Рё side-blur РєР°РЅР°Р». */
    private static final boolean BLUR_ENABLED = true;
    /** РњР°РєСЃРёРјР°Р»СЊРЅР°СЏ РёРЅС‚РµРЅСЃРёРІРЅРѕСЃС‚СЊ Р±Р»СЋСЂР° РЅР° РїРёРєРµ (0..1). */
    private static final float BLUR_STRENGTH = 0.55F;

    /** РЎРєРѕСЂРѕСЃС‚СЊ РёРЅС‚РµСЂРїРѕР»СЏС†РёРё factor в†’ target. РњРµРЅСЊС€Рµ = РїР»Р°РІРЅРµРµ. */
    private static final float FX_SMOOTHING = 0.06F;
    /** РЎРєРѕСЂРѕСЃС‚СЊ СЃРіР»Р°Р¶РёРІР°РЅРёСЏ СЃР°РјРѕР№ rawSpeed. */
    private static final float SPEED_SMOOTHING = 0.20F;

    // === РџРёРєРѕРІС‹Рµ СЃРєРѕСЂРѕСЃС‚Рё РІ Р±Р»РѕРєР°С…/С‚РёРє (РїСЂРё РЅРёС… factor = 1.0) ===
    private static final float PEAK_BOAT     = 1.10F; // Р»С‘Рґ = РїРёРє, РІРѕРґР° = ~30%
    private static final float PEAK_HORSE    = 0.50F; // РїРѕР»РЅС‹Р№ РіР°Р»РѕРї
    private static final float PEAK_CAMEL    = 0.45F; // РґСЌС€
    private static final float PEAK_MINECART = 0.80F; // powered rails РЅР° РјР°РєСЃРёРјСѓРјРµ
    private static final float PEAK_DEFAULT  = 0.60F; // РјРѕРґРґРѕРІС‹Рµ РўРЎ

    // ============================================================
    //                       РЎРћРЎРўРћРЇРќРР•
    // ============================================================

    private static float smoothedSpeed = 0.0F;
    private static float factor = 0.0F;
    private static float prevFactor = 0.0F;
    private static Class<?> prevVehicleType = null;

    private CameraVehicleSpeedFx() {}

    // ============================================================
    //                     РџРЈР‘Р›РР§РќРћР• API
    // ============================================================

    /** РќРѕСЂРјР°Р»РёР·РѕРІР°РЅРЅС‹Р№ С„Р°РєС‚РѕСЂ 0..1 СЃ СѓС‡С‘С‚РѕРј partialTick. Р’ 3rd person в†’ 0. */
    public static float getFactor(float partialTick) {
        if (!CameraViewUtils.isFirstPerson()) return 0.0F;
        return Mth.lerp(Mth.clamp(partialTick, 0.0F, 1.0F), prevFactor, factor);
    }

    /** РЎС‹СЂРѕР№ С‚РµРєСѓС‰РёР№ С„Р°РєС‚РѕСЂ (РґР»СЏ tick-Р»РѕРіРёРєРё). Р’ 3rd person в†’ 0. */
    public static float getFactorRaw() {
        if (!CameraViewUtils.isFirstPerson()) return 0.0F;
        return factor;
    }

    /**
     * Р’РєР»Р°Рґ РІ FOV-РјРЅРѕР¶РёС‚РµР»СЊ.
     * Р’РѕР·РІСЂР°С‰Р°РµС‚ Р·РЅР°С‡РµРЅРёРµ РґР»СЏ РїСЂРёР±Р°РІР»РµРЅРёСЏ Рє РёС‚РѕРіРѕРІРѕРјСѓ РјРЅРѕР¶РёС‚РµР»СЋ FOV.
     * РќР° РїРёРєРµ СЃРєРѕСЂРѕСЃС‚Рё РІРµСЂРЅС‘С‚ ~+0.12 в†’ РёС‚РѕРіРѕРІС‹Р№ FOV = base * 1.12.
     * Р’ 3rd person в†’ 0.
     */
    public static float getFovBoost(float partialTick) {
        if (!ENABLED || !FOV_ENABLED) return 0.0F;
        if (!CameraViewUtils.isFirstPerson()) return 0.0F;
        float f = getFactor(partialTick);
        // РњСЏРіРєРёР№ ease-in: РЅРёР·РєРёРµ СЃРєРѕСЂРѕСЃС‚Рё РїРѕС‡С‚Рё РЅРµ РѕС‚РґР°Р»СЏСЋС‚.
        float curved = (float) Math.pow(f, 1.5);
        return curved * FOV_STRENGTH;
    }

    /**
     * РРЅС‚РµРЅСЃРёРІРЅРѕСЃС‚СЊ side-blur (0..1) РґР»СЏ РїРѕРґРјРµС€РёРІР°РЅРёСЏ РІ С€РµР№РґРµСЂ.
     * Р’ 3rd person в†’ 0.
     */
    public static float getBlurIntensity(float partialTick) {
        if (!ENABLED || !BLUR_ENABLED) return 0.0F;
        if (!CameraViewUtils.isFirstPerson()) return 0.0F;
        float f = getFactor(partialTick);
        // РљРІР°РґСЂР°С‚РёС‡РЅР°СЏ РєСЂРёРІР°СЏ: РґРѕ 50% СЃРєРѕСЂРѕСЃС‚Рё РїРѕС‡С‚Рё РЅРµС‚ Р±Р»СЋСЂР°.
        float curved = f * f;
        return curved * BLUR_STRENGTH;
    }

    // ============================================================
    //                          РўРРљ
    // ============================================================

    public static void tick() {
        try {
            prevFactor = factor;

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null || mc.isPaused()) {
                decay();
                return;
            }
            if (!ENABLED) { decay(); return; }

            // рџЋҐ Р’ 3rd person вЂ” РјСЏРіРєРѕ РіР°СЃРёРј СЌС„С„РµРєС‚, С‡С‚РѕР±С‹ РїСЂРё РІРѕР·РІСЂР°С‚Рµ РІ 1st person
            // РѕРЅ РїР»Р°РІРЅРѕ РЅР°СЂРѕСЃС‚Р°Р» СЃ РЅСѓР»СЏ, Р° РЅРµ В«РїСЂС‹РіР°Р»В» РѕС‚ СЂР°РЅРµРµ РЅР°РєРѕРїР»РµРЅРЅРѕР№ СЃРєРѕСЂРѕСЃС‚Рё.
            if (!CameraViewUtils.isFirstPerson()) {
                decay();
                // С‚РёРї РўРЎ РІСЃС‘ Р¶Рµ Р·Р°РїРѕРјРёРЅР°РµРј, С‡С‚РѕР±С‹ РЅРµ Р±С‹Р»Рѕ Р»РѕР¶РЅРѕРіРѕ В«РїРµСЂРµСЃРµР» РЅР° РґСЂСѓРіРѕР№В»
                Entity v = mc.player.getVehicle();
                prevVehicleType = (v != null) ? v.getClass() : null;
                return;
            }

            // Р—Р°РІСЏР·РєР° РЅР° master switch РёРЅРµСЂС†РёРё С‚СЂР°РЅСЃРїРѕСЂС‚Р°
            if (!ClientConfig.VEHICLE_INERTIA_ENABLED.get()) {
                decay();
                return;
            }

            Player player = mc.player;
            Entity vehicle = player.getVehicle();
            if (vehicle == null) {
                decay();
                prevVehicleType = null;
                return;
            }

            // РџРµСЂРµСЃРµР» РЅР° РґСЂСѓРіРѕР№ С‚СЂР°РЅСЃРїРѕСЂС‚ в†’ Р¶С‘СЃС‚РєРёР№ СЃР±СЂРѕСЃ СЃРіР»Р°Р¶РёРІР°РЅРёСЏ
            Class<?> currentType = vehicle.getClass();
            if (prevVehicleType != null && prevVehicleType != currentType) {
                smoothedSpeed = 0.0F;
                factor = 0.0F;
            }
            prevVehicleType = currentType;

            // === РЎС‹СЂР°СЏ РіРѕСЂРёР·РѕРЅС‚Р°Р»СЊРЅР°СЏ СЃРєРѕСЂРѕСЃС‚СЊ ===
            Vec3 motion = vehicle.getDeltaMovement();
            double rawSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

            // === РџРёРє РїРѕРґ С‚РёРї С‚СЂР°РЅСЃРїРѕСЂС‚Р° ===
            float peak = getPeakSpeedFor(vehicle);
            if (peak <= 0.0F) {
                decay();
                return;
            }

            // === Р­С‚Р°Рї 1: СЃРіР»Р°Р¶РёРІР°РµРј СЃС‹СЂСѓСЋ СЃРєРѕСЂРѕСЃС‚СЊ ===
            smoothedSpeed += ((float) rawSpeed - smoothedSpeed) * SPEED_SMOOTHING;

            // === Р­С‚Р°Рї 2: СЃС‡РёС‚Р°РµРј С†РµР»РµРІРѕР№ С„Р°РєС‚РѕСЂ Рё РїР»Р°РІРЅРѕ С‚СЏРЅРµРјСЃСЏ Рє РЅРµРјСѓ ===
            float target = Mth.clamp(smoothedSpeed / peak, 0.0F, 1.0F);

            // РђСЃРёРјРјРµС‚СЂРёСЏ: СЂР°Р·РіРѕРЅ С‡СѓС‚СЊ РјРµРґР»РµРЅРЅРµРµ, С‚РѕСЂРјРѕР¶РµРЅРёРµ Р±С‹СЃС‚СЂРµРµ.
            float lerpSpeed = (target > factor) ? FX_SMOOTHING : FX_SMOOTHING * 1.4F;
            factor += (target - factor) * lerpSpeed;

            if (factor < 0.001F) factor = 0.0F;
            if (Float.isNaN(factor) || Float.isInfinite(factor)) factor = 0.0F;

        } catch (Throwable t) {
            t.printStackTrace();
            smoothedSpeed = 0.0F;
            factor = 0.0F;
            prevFactor = 0.0F;
            prevVehicleType = null;
        }
    }

    // ============================================================
    //                       Р’РќРЈРўР Р•РќРќР•Р•
    // ============================================================

    /** РџР»Р°РІРЅС‹Р№ СЃРїР°Рґ РїСЂРё РѕС‚СЃСѓС‚СЃС‚РІРёРё С‚СЂР°РЅСЃРїРѕСЂС‚Р° / РІ 3rd person. */
    private static void decay() {
        smoothedSpeed *= 0.85F;
        factor *= 0.88F;
        if (smoothedSpeed < 0.001F) smoothedSpeed = 0.0F;
        if (factor < 0.001F) factor = 0.0F;
    }

    /** РџРёРєРѕРІР°СЏ СЃРєРѕСЂРѕСЃС‚СЊ bps РїРѕРґ С‚РёРї РўРЎ. 0 = СЌС„С„РµРєС‚ РѕС‚РєР»СЋС‡С‘РЅ РґР»СЏ СЌС‚РѕРіРѕ РўРЎ. */
    private static float getPeakSpeedFor(Entity vehicle) {
        if (vehicle instanceof Boat)             return PEAK_BOAT;
        if (vehicle instanceof AbstractMinecart) return PEAK_MINECART;
        if (vehicle instanceof Camel)            return PEAK_CAMEL;
        if (vehicle instanceof AbstractHorse)    return PEAK_HORSE;
        if (vehicle instanceof Pig || vehicle instanceof Strider) {
            return 0.0F; // РѕС‚РєР»СЋС‡Р°РµРј
        }
        return PEAK_DEFAULT;
    }
}