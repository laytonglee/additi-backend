package groupproject.additibackend.khqr;

public enum KHQRCurrency {
    USD("840"),
    KHR("116");

    private final String code;

    KHQRCurrency(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return this.name();
    }
}
