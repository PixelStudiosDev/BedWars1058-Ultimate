package me.cubecrafter.ultimate.ultimates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.config.Configuration;

@RequiredArgsConstructor
public enum Ultimate {

    KANGAROO(Configuration.KANGAROO_DISPLAYNAME.getAsString()),
    SWORDSMAN(Configuration.SWORDSMAN_DISPLAYNAME.getAsString()),
    HEALER(Configuration.HEALER_DISPLAYNAME.getAsString()),
    FROZO(Configuration.FROZO_DISPLAYNAME.getAsString()),
    BUILDER(Configuration.BUILDER_DISPLAYNAME.getAsString()),
    DEMOLITION(Configuration.DEMOLITION_DISPLAYNAME.getAsString()),
    GATHERER(Configuration.GATHERER_DISPLAYNAME.getAsString());

    @Getter private final String name;

}
