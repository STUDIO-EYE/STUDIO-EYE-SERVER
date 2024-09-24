package studio.studioeye.domain.request.dao;

public class RequestCountImpl implements RequestCount{
    private Integer year;
    private Integer month;
    private Long count;

    public RequestCountImpl(Integer year, Integer month, Long count) {
        this.year = year;
        this.month = month;
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
    public Long getRequestCount() {
        return count;
    }
}
