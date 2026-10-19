package com.mevdz.ghst;

import com.mevdz.ghst.modules.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.add(new AimAssist());
        modules.add(new AutoClicker());
        modules.add(new Reach());
        modules.add(new EntityESP());
        modules.add(new Fullbright());
        modules.add(new ToggleSprint());
    }
}
