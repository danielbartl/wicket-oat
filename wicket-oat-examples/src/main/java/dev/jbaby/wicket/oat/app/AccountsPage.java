package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.table.OatNumberColumn;
import dev.jbaby.wicket.oat.components.tree.OatTableTree;
import dev.jbaby.wicket.oat.components.tree.OatTree;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

/**
 * An OatTree of document folders that selects a folder, and an OatTableTree: a chart
 * of accounts with each account's number and balance, sub-accounts indented.
 */
public class AccountsPage extends BasePage {

    /** A tree node: a folder or an account, with its children. Records compare by content, so names are unique. */
    public record Node(String name, String number, BigDecimal own, List<Node> children) implements Serializable {

        static Node of(String name, Node... children) {
            return new Node(name, null, null, List.of(children));
        }

        static Node account(String number, String name, String balance) {
            return new Node(name, number, new BigDecimal(balance), List.of());
        }

        static Node group(String number, String name, Node... children) {
            return new Node(name, number, null, List.of(children));
        }

        /** An account's balance, a group's the sum of its accounts. */
        BigDecimal balance() {
            return own != null ? own : children.stream().map(Node::balance).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        boolean isLeaf() {
            return children.isEmpty();
        }
    }

    private static final List<Node> FOLDERS = List.of(
            Node.of("Customers", Node.of("ACME GmbH", Node.of("ACME contracts"), Node.of("ACME invoices")),
                    Node.of("Globex", Node.of("Globex invoices"))),
            Node.of("Finance", Node.of("2025", Node.of("Q3 2025"), Node.of("Q4 2025")),
                    Node.of("2026", Node.of("Q1 2026"), Node.of("Q2 2026"), Node.of("Q3 2026"))),
            Node.of("Templates"));

    private static final List<Node> ACCOUNTS = List.of(
            Node.group("1", "Assets",
                    Node.account("1000", "Cash", "1250.00"),
                    Node.group("1200", "Bank accounts",
                            Node.account("1210", "Checking", "48210.35"),
                            Node.account("1220", "Savings", "120000.00")),
                    Node.account("1400", "Receivables", "31544.80")),
            Node.group("2", "Liabilities",
                    Node.account("3300", "Payables", "-18320.10"),
                    Node.account("3500", "Loans", "-60000.00")),
            Node.group("4", "Revenue",
                    Node.account("4000", "Products", "-212400.00"),
                    Node.account("4400", "Services", "-86250.00")),
            Node.group("6", "Expenses",
                    Node.account("6000", "Salaries", "142000.00"),
                    Node.account("6300", "Rent", "24000.00"),
                    Node.account("6800", "Software", "9965.00")));

    private final IModel<String> folder = Model.of("Nothing selected");

    public AccountsPage() {
        add(new OatTree<>("folders", new NodeProvider(FOLDERS))
                .setLabel(Node::name)
                .setIcon(node -> "folder")
                .onSelect((target, node) -> {
                    folder.setObject(node.name());
                    target.add(get("selectedFolder"));
                }));
        add(new Label("selectedFolder", folder).setOutputMarkupId(true));

        List<IColumn<Node, String>> columns = List.of(
                new TreeColumn<>(Model.of("Account")),
                new LambdaColumn<>(Model.of("Number"), Node::number),
                new OatNumberColumn<Node, String>(Model.of("Balance"), Node::balance).setCurrency(Currency.getInstance("EUR")));
        OatTableTree<Node, String> accounts = new OatTableTree<>("accounts", columns, new NodeProvider(ACCOUNTS), 50)
                .setLabel(Node::name);
        // Open the top level, so the chart starts with its groups' accounts
        ACCOUNTS.forEach(accounts::expand);
        add(accounts);
    }

    private static final class NodeProvider implements ITreeProvider<Node> {
        private final List<Node> roots;

        NodeProvider(List<Node> roots) {
            this.roots = roots;
        }

        @Override
        public Iterator<? extends Node> getRoots() {
            return roots.iterator();
        }

        @Override
        public boolean hasChildren(Node node) {
            return !node.isLeaf();
        }

        @Override
        public Iterator<? extends Node> getChildren(Node node) {
            return node.children().iterator();
        }

        @Override
        public IModel<Node> model(Node node) {
            return Model.of(node);
        }

        @Override
        public void detach() {
        }
    }

}
