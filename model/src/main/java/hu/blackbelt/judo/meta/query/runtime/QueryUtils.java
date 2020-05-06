package hu.blackbelt.judo.meta.query.runtime;

import hu.blackbelt.judo.meta.query.Join;
import hu.blackbelt.judo.meta.query.Select;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.query.runtime.StringUtils.leftPad;

public class QueryUtils {

    private static final Logger log = LoggerFactory.getLogger(QueryUtils.class);

    public static final String JOIN_ALIAS_FORMAT = "j{0,number,00}";
    private static final String SUBSELECT_ALIAS_FORMAT = "ss{0,number,00}";

    public static EList<Join> getAllJoinsOfSelect(final Select select) {
        return collectAllJoins(ECollections.newBasicEList(), select.getJoins());
    }

    private static EList<Join> collectAllJoins(final EList<Join> found, final Collection<Join> processing) {
        if (processing.isEmpty()) {
            return found;
        }

        final Collection<Join> newTargets = processing.stream().filter(t -> !found.contains(t)).collect(Collectors.toList());
        found.addAll(newTargets);

        return collectAllJoins(found, newTargets.stream().flatMap(t -> t.getJoins().stream()).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public static String formatSelect(final Select select) {
        return formatSelect(select, 0, ECollections.emptyEList());
    }

    private static String formatSelect(final Select select, final int level, final EList<Select> selects) {
        if (selects.contains(select)) {
            return pad(level) + "... (SELECT FROM " + select.getFrom().getName() + " TO " + select.getMainTarget().getIndex() + ")\n";
        }

        final EList<Select> visited = ECollections.newBasicEList(selects);
        visited.add(select);

        return pad(level) + "SELECT\n" +
                pad(level) + "  FEATURES=" + select.getFeatures() + "\n" +
                pad(level) + "  FROM=" + select.getFrom().getName() + " AS " + select.getAlias() + "\n" +
                pad(level) + "  JOINING=" + select.getAllJoins() + "\n" +
                pad(level) + "  TO=" + select.getTargets() + "\n" +
                (select.getFilters().isEmpty() ? "" : pad(level) + "  WHERE=" + select.getFilters() + "\n") +
                (select.getOrderBys().isEmpty() ? "" : pad(level) + "  ORDER BY=" + select.getOrderBys() + "\n") +
                (select.getSubSelects().isEmpty() ? "" : select.getSubSelects().stream().map(s -> pad(level) + (s.getSelect().isAggregated() ? "  AGGREGATE " : "  TRAVERSE ") + s +
                        "\n" + formatSelect(s.getSelect(), level + 1, visited) +
                        (!s.getSelect().isAggregated() && !s.getPartner().getOrderBys().isEmpty() ? "    ORDER BY=" + s.getPartner().getOrderBys() + "\n" : "")).collect(Collectors.joining())) +
                select.getAllJoins().stream().map(join -> join.getSubSelects().stream().map(s ->
                        pad(level) + (s.getSelect().isAggregated() ? "  AGGREGATE " : "  TRAVERSE ") + s +
                                "\n" + formatSelect(s.getSelect(), level + 1, visited) +
                                (!s.getSelect().isAggregated() && !s.getPartner().getOrderBys().isEmpty() ? "    ORDER BY=" + s.getPartner().getOrderBys() + "\n" : "")).collect(Collectors.joining()))
                        .collect(Collectors.joining());
    }

    private static String pad(int level) {
        return leftPad("", level * 4, " ");
    }

    public static String getNextJoinAlias(final AtomicInteger nextSubSelectIndex) {
        return MessageFormat.format(JOIN_ALIAS_FORMAT, nextSubSelectIndex.incrementAndGet());
    }

    public static String getNextSubSelectAlias(final AtomicInteger nextSubSelectIndex) {
        return MessageFormat.format(SUBSELECT_ALIAS_FORMAT, nextSubSelectIndex.incrementAndGet());
    }
}
