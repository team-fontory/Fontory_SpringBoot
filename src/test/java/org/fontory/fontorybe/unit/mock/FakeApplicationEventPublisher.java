package org.fontory.fontorybe.unit.mock;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeApplicationEventPublisher implements ApplicationEventPublisher {
    private final List<Object> events = new ArrayList<>();

    @Override
    public void publishEvent(ApplicationEvent event) {
        events.add(event);
    }

    @Override
    public void publishEvent(Object event) {
        events.add(event);
    }

    public List<Object> getPublishedEvents() {
        return Collections.unmodifiableList(events);
    }
}
