package peterfajdiga.fastdraw.categoryorder;

import java.util.Comparator;

import peterfajdiga.fastdraw.PrefMap;

public class CategoryComparator implements Comparator<String> {

    public static final int UNORDERED = Integer.MAX_VALUE;

    private final PrefMap categoryMap;

    public CategoryComparator(final PrefMap categories) {
        categoryMap = categories;
    }

    @Override
    public int compare(String category1, String category2) {
        final int order_category1 = categoryMap.getIntCached(category1, UNORDERED);
        final int order_category2 = categoryMap.getIntCached(category2, UNORDERED);
        final int orderDiff = order_category1 - order_category2;
        if (orderDiff == 0) {
            return category1.compareTo(category2);
        } else {
            return orderDiff;
        }
    }
}
