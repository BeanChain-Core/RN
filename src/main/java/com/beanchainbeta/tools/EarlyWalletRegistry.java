package com.beanchainbeta.tools;

import java.util.Set;

public class EarlyWalletRegistry {

    private static final Set<String> earlyWallets = Set.of(
        "BEANX:0xFAUCETWALLET",
        "BEANX:0xEARLYWALLET",
        "BEANX:0xSTAKEREWARD",
        "BEANX:0xNODEREWARD",
        "BEANX:0x1c8496175b3f4802e395db5fab4dd66e09c431b2",
        "BEANX:0xLIQUIDITY"
    );

    public static boolean isExcludedFromRewards(String address) {
        return earlyWallets.contains(address);
    }
    
}
