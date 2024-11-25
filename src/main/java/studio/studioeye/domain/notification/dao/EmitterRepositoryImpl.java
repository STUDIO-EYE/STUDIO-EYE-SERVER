package studio.studioeye.domain.notification.dao;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Object> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(Long emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        return null;
    }
    @Override
    public void saveEventCache(Long eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }
    @Override
    public void deleteById(Long id) {
        emitters.remove(id);
    }
    @Override
    public void deleteAllEmitterStartWithId(Long memberId) {
        emitters.forEach(
                (key, emitter) -> {
                    if (key.toString().startsWith(memberId.toString())) {
                        emitters.remove(key);
                    }
                }
        );
    }

    @Override
    public void deleteAllEventCacheStartWithId(Long memberId) {
        eventCache.forEach(
                (key, emitter) -> {
                    if (key.toString().startsWith(memberId.toString())) {
                        eventCache.remove(key);
                    }
                }
        );
    }

    @Override
    public SseEmitter get(Long id) {
        return emitters.get(id);
    }
    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}