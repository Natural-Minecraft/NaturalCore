package id.naturalsmp.naturalcore.season;

public enum Season {
    SPRING("🌸", "SPRING"),
    SUMMER("☀", "SUMMER"),
    AUTUMN("🍂", "AUTUMN"),
    WINTER("❄", "WINTER");

    private final String icon;
    private final String configKey;

    Season(String icon, String configKey) {
        this.icon = icon;
        this.configKey = configKey;
    }

    public String getIcon() {
        return icon;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Season next() {
        switch (this) {
            case SPRING:
                return SUMMER;
            case SUMMER:
                return AUTUMN;
            case AUTUMN:
                return WINTER;
            case WINTER:
                return SPRING;
            default:
                return SPRING;
        }
    }
}
