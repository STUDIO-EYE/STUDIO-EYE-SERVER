//package studio.studioeye.domain.user.controller;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import studio.studioeye.global.config.FeignConfig;
//
//import java.util.List;
//
//@FeignClient(name = "user-service", url = "${user-service.url}", configuration = FeignConfig.class)
//public interface UserFeignClient {
//    @GetMapping("/users-id/all")
//    List<Long> getAllApprovedUserIds();
//}
