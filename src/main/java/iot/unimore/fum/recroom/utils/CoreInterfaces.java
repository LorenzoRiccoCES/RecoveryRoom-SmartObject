package iot.unimore.fum.recroom.utils;

public enum CoreInterfaces {
    CORE_LL("core.ll"),
    CORE_B("core.b"),
    CORE_LB("core.lb"),
    CORE_S("core.s"),
    CORE_P("core.p"),
    CORE_RP("core.rp"),
    CORE_A("core.a");

    private String value;

    CoreInterfaces(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
