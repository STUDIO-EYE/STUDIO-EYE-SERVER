package studio.studioeye.domain.request.dao;

public class RequestCategoryCountImpl implements RequestCategoryCount{
    private Integer year;
    private Integer month;
    private String category;
    private Long count;

    public RequestCategoryCountImpl(Integer year, Integer month, String category, Long count) {
        this.year = year;
        this.month = month;
        this.category = category;
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
    public String getCategory() {
        return category;
    }

    @Override
    public Long getRequestCount() {
        return count;
    }
}