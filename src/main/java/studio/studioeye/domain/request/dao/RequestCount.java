package studio.studioeye.domain.request.dao;

import studio.studioeye.domain.request.domain.State;

public interface RequestCount {
    Integer getYear();
    Integer getMonth();
    Long getRequestCount();
    String getCategory();
    State getState();
}
