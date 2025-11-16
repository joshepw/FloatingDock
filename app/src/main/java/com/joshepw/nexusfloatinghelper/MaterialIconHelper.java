package com.joshepw.nexusfloatinghelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MaterialIconHelper {
    public static List<String> getAllIconNames() {
        // Obtener todos los iconos del mapeo y ordenarlos alfabéticamente
        Set<String> iconSet = MaterialSymbolsMapper.getAllIconNames();
        List<String> iconList = new ArrayList<>(iconSet);
        Collections.sort(iconList);
        return iconList;
    }
}

