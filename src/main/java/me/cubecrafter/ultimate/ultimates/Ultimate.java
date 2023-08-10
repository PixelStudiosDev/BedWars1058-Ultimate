package me.cubecrafter.ultimate.ultimates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.config.Config;

@Getter
@RequiredArgsConstructor
public enum Ultimate {

    KANGAROO(Config.KANGAROO_DISPLAYNAME.getAsString()),
    SWORDSMAN(Config.SWORDSMAN_DISPLAYNAME.getAsString()),
    HEALER(Config.HEALER_DISPLAYNAME.getAsString()),
    FROZO(Config.FROZO_DISPLAYNAME.getAsString()),
    BUILDER(Config.BUILDER_DISPLAYNAME.getAsString()),
    DEMOLITION(Config.DEMOLITION_DISPLAYNAME.getAsString()),
    GATHERER(Config.GATHERER_DISPLAYNAME.getAsString());

    private final String name;

}
