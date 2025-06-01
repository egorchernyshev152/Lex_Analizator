package parser;

import java.util.*;

/**
 * Узел синтаксического дерева (Parse Tree).
 */
public class ParseTreeNode {
    public final String name;
    public final boolean isTerminal;
    public final List<ParseTreeNode> children = new ArrayList<>();

    public ParseTreeNode(String name, boolean isTerminal) {
        this.name = name;
        this.isTerminal = isTerminal;
    }

    public void addChild(ParseTreeNode child) {
        children.add(child);
    }

    public void print(String indent) {
        System.out.println(indent + name);
        for (ParseTreeNode child : children) {
            child.print(indent + "  ");
        }
    }
}
