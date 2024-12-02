package studio.studioeye.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import studio.studioeye.domain.user.domain.User;
import studio.studioeye.domain.user.dto.response.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findById(Long userId);

    default UserResponse findUserResponseByUserId(Long userId) {
        Optional<User> userOptional = findById(userId);
        User user = userOptional.get();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.isApproved());
    }

    default UserResponse findUserResponseByEmail(String email) {
        Optional<User> userOptional = findByEmail(email);
        User user = userOptional.get();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.isApproved());
    }

    @Query("SELECT u.id FROM User u WHERE u.isApproved = true")
    List<Long> getAllApprovedUserIds();

    default List<UserResponse> findAllUsers(){
        List<User> userList = findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        for(User user : userList) {
            userResponseList.add (new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPhoneNumber(),
                    user.getCreatedAt(),
                    user.isApproved()));
        }
        return userResponseList;
    }
}
