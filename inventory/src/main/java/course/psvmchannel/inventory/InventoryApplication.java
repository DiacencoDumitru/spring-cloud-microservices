package course.psvmchannel.inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class InventoryApplication {

    private static final int DEFAULT_STOCK_PER_SKU = 50;

    private final Map<String, Integer> skuToAvailable = new ConcurrentHashMap<>();
    private final Map<UUID, String> reservationIdToSku = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(@RequestBody ReservationRequest request) {
        String sku = request.getSku();
        if (sku == null || sku.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        skuToAvailable.putIfAbsent(sku, DEFAULT_STOCK_PER_SKU);
        synchronized (this) {
            int available = skuToAvailable.get(sku);
            if (available <= 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            UUID id = UUID.randomUUID();
            skuToAvailable.put(sku, available - 1);
            reservationIdToSku.put(id, sku);
            return ResponseEntity.ok(new ReservationResponse(id.toString()));
        }
    }

    @DeleteMapping("/reservations/{reservationId}")
    public synchronized ResponseEntity<Void> release(@PathVariable String reservationId) {
        UUID id;
        try {
            id = UUID.fromString(reservationId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        String sku = reservationIdToSku.remove(id);
        if (sku == null) {
            return ResponseEntity.notFound().build();
        }
        skuToAvailable.merge(sku, 1, Integer::sum);
        return ResponseEntity.noContent().build();
    }

    public static final class ReservationRequest {
        private String sku;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }
    }

    public static final class ReservationResponse {
        private final String reservationId;

        public ReservationResponse(String reservationId) {
            this.reservationId = reservationId;
        }

        public String getReservationId() {
            return reservationId;
        }
    }
}
