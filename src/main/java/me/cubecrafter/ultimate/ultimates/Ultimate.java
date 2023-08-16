package me.cubecrafter.ultimate.ultimates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.config.Config;

@Getter
@RequiredArgsConstructor
public enum Ultimate {

    KANGAROO(Config.KANGAROO_DISPLAYNAME.asString()),
    SWORDSMAN(Config.SWORDSMAN_DISPLAYNAME.asString()),
    HEALER(Config.HEALER_DISPLAYNAME.asString()),
    FROZO(Config.FROZO_DISPLAYNAME.asString()),
    BUILDER(Config.BUILDER_DISPLAYNAME.asString()),
    DEMOLITION(Config.DEMOLITION_DISPLAYNAME.asString()),
    GATHERER(Config.GATHERER_DISPLAYNAME.asString());

    private final String name;

}
