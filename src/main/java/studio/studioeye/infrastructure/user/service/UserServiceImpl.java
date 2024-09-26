//package studio.studioeye.domain.user.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import studio.studioeye.domain.user.controller.UserFeignClient;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserServiceImpl {
//
//    private final UserFeignClient userFeignClient;
//
//    public List<Long> getAllApprovedUserIds() {
//        try {
//            return userFeignClient.getAllApprovedUserIds();
//        }
//        catch (Exception e){
//            return null;
//        }
//    }
//
//}
