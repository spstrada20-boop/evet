package com.mevdz.ghst;

import java.util.Locale;

public class Setting {
    public enum Type { SLIDER, BOOL, COLOR }

    public final String name;
    public final Type type;
    public final double min, max, step;
    private double value;
    private int rgb;
    private int key; // 0 = bagli degil (sadece BOOL ayarlar icin)

    public Setting(String name, double def, double min, double max, double step) {
        this.name = name; this.type = Type.SLIDER;
        this.min = min; this.max = max; this.step = step;
        set(def);
    }

    private Setting(String name, Type type) {
        this.name = name; this.type = type;
        this.min = 0; this.max = 1; this.step = 1;
    }

    public static Setting bool(String name, boolean def) {
        Setting s = new Setting(name, Type.BOOL);
        s.value = def ? 1 : 0;
        return s;
    }

    public static Setting color(String name, int rgb) {
        Setting s = new Setting(name, Type.COLOR);
        s.rgb = rgb & 0xFFFFFF;
        return s;
    }

    // ---- slider / bool ----
    public double get()     { return value; }
    public float getF()     { return (float) value; }
    public boolean isOn()   { return value >= 0.5; }
    public void toggle()    { value = isOn() ? 0 : 1; }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        if (step > 0) v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    // ---- color ----
    public int getColor() { return rgb; }
    public void setColor(int c) { rgb = c & 0xFFFFFF; }

    /** ch: 0 = R, 1 = G, 2 = B */
    public int getChannel(int ch) { return (rgb >> (16 - 8 * ch)) & 255; }

    public void setChannel(int ch, int v) {
        v = Math.max(0, Math.min(255, v));
        int shift = 16 - 8 * ch;
        rgb = (rgb & ~(255 << shift)) | (v << shift);
    }

    // ---- keybind ----
    public int getKey() { return key; }
    public void setKey(int k) { key = k; }

    // ---- gosterim / kayit ----
    public String valueStr() {
        if (type == Type.BOOL) return isOn() ? "ON" : "OFF";
        if (type == Type.COLOR) return String.format("#%06X", rgb);
        if (step >= 1) return String.valueOf((int) Math.round(value));
        return String.format(Locale.US, "%.1f", value);
    }

    public String serialize() {
        return type == Type.COLOR ? String.valueOf(rgb) : String.valueOf(value);
    }

    public void deserialize(String s) {
        try {
            if (type == Type.COLOR) setColor(Integer.parseInt(s.trim()));
            else set(Double.parseDouble(s.trim()));
        } catch (NumberFormatException ignored) { }
    }
}
