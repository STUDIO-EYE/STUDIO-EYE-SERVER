package studio.studioeye.domain.request.dao;

import studio.studioeye.domain.request.domain.State;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class RequestCountImpl implements RequestCount{
    private Integer year;
    private Integer month;
    private Long count;
    private String category;
    @Enumerated(EnumType.STRING)
    private State state;

    public RequestCountImpl(Integer year, Integer month, Long count, String category, State state) {
        this.year = year;
        this.month = month;
        this.count = count;
        this.category = category;
        this.state = state;
    }
    @Override
    public Integer getYear() {
        return year;
    }

    @Override
    public Integer getMonth() {
        return month;
    }

    @Override
    public Long getRequestCount() {
        return count;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public State getState() {
        return state;
    }
}
