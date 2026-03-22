package course.psvmchannel.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import course.psvmchannel.order.dto.ReservationRequest;
import course.psvmchannel.order.dto.ReservationResponse;

@FeignClient(name = "inventory", url = "http://localhost:8004/")
public interface InventoryFeignClient {

    @PostMapping("/reservations")
    ReservationResponse reserve(@RequestBody ReservationRequest request);

    @DeleteMapping("/reservations/{reservationId}")
    void release(@PathVariable("reservationId") String reservationId);
}
