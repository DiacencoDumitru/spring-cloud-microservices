package course.psvmchannel.order.dto;

public class ReservationRequest {

    private String sku;

    public ReservationRequest() {
    }

    public ReservationRequest(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
