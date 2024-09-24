package studio.studioeye.domain.request.dao;


public interface RequestCategoryCount {
    Integer getYear();
    Integer getMonth();
    String getCategory();
    Long getRequestCount();
}