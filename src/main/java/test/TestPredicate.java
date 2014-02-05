package test;

import java.util.Map.Entry;
import java.util.Set;

import com.hazelcast.query.IndexAwarePredicate;
import com.hazelcast.query.impl.QueryContext;
import com.hazelcast.query.impl.QueryableEntry;

public class TestPredicate implements IndexAwarePredicate {

    private String value;
    private boolean didApply;

    public TestPredicate(String value) {
        this.value = value;
    }
    
    public boolean apply(Entry mapEntry) {
        didApply = true;
        System.out.println("matching entry " + mapEntry.getValue() + " with " + value);
        Data data = (Data) mapEntry.getValue();
        return data.getAttr1().equals(value);
    }

    public Set<QueryableEntry> filter(QueryContext queryContext) {
        System.out.println("quering index for " + value);
        return queryContext.getIndex("attr1").getRecords(value);
    }

    public boolean isIndexed(QueryContext queryContext) {
        System.out.println("isIndexed called.");
        return queryContext.getIndex("attr1") != null;
    }

    public boolean didApply() {
        return didApply;
    }

}
