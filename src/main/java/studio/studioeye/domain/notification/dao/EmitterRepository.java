package studio.studioeye.domain.notification.dao;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
@Repository
// Emitter가 어떤 데이터와 연결되어있는지, 어떤 이벤트들이 현재까지 발생했는지 저장해야하므로 EmitterRepository 생성
public interface EmitterRepository {
    SseEmitter save(Long emitterId, SseEmitter sseEmitter);
    void saveEventCache(Long emitterId, Object event);
    void deleteById(Long id);
    void deleteAllEmitterStartWithId(Long memberId);
    void deleteAllEventCacheStartWithId(Long memberId);

    SseEmitter get(Long id);
    Collection<SseEmitter> getAllEmitters();
}
