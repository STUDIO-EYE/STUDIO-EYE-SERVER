package studio.studioeye.domain.request.dao;

import lombok.Getter;

@Getter
public class RequestStateCountImpl implements RequestStateCount{
    private Integer year;
    private Integer month;
    private String state;
    private Long count;
    public RequestStateCountImpl(Integer year, Integer month, String state, Long count) {
        this.year = year;
        this.month = month;
        this.state = state;
        this.count = count;
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
    public String getState() {
        return state;
    }
    @Override
    public Long getRequestCount() {
        return count;
    }
}
