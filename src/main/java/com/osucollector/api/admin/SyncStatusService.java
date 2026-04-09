package com.osucollector.api.admin;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SyncStatusService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);

    public boolean isSyncRunning() {
        return syncRunning.get();
    }

    public boolean startSync() {
        // check if already running
        return syncRunning.compareAndSet(false, true);
    }

    public void endSync() {
        syncRunning.set(false);
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5min TO
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void sendProgress(int updated, int total) {
        sendEvent("progress", updated + "/" + total);
    }

    public void sendComplete(int updated, int failed) {
        sendEvent("complete", updated + ":" + failed);
    }

    public void sendError(String message) {
        sendEvent("error", message);
    }

    private void sendEvent(String name, String data) {
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(name).data(data));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }
}