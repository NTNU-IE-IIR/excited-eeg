package no.ntnu.stud.avikeyb.backend.layouts;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.ntnu.stud.avikeyb.backend.Symbol;

import static no.ntnu.stud.avikeyb.backend.Symbol.*;

/**
 * Defines the selection tree for the binary search layout.
 * <p>
 * The tree is hard coded to be able to divide the selection into groups that make sense
 * visually. This should make it easier for the user to remember or pre-calculate the steps
 * needed to select a given item in the layout.
 */
public class BinarySearchTreeDefinition {

    /**
     * Builds a new selection tree with the provided suggestions included in the tree.
     *
     * @param suggestions a list of suggestions to include in the tree
     * @return a selection tree
     */
    public static Node buildBinarySearchLayoutTree(final List<String> suggestions) {

        /**
         * The format is as follows:
         *
         * d = defines a new node dynamic node (includes suggestions)
         * s = defines a symbol list
         * n = defines a new static node (Nodes of this type does never change. This can be used to optimize the tree later by caching the static subtrees)
         * l = defines a and create two leaf nodes with the two provided symbols. (The end of the branch)
         *
         * The first line is the root. The symbols on the first line is all the symbols that are
         * in the tree. The symbol list that is defines for each node is all the symbols that
         * are in the nodes left and right sub trees combined. This can be used to highlight items
         * in the layout that can still be selected, and gray out the others.
         *
         * Each indent level below a node should have two sub nodes. The first is the left sub tree
         * and the other is the right subtree. E.g. the node on line 92 defines the left subtree of
         * the root node and the node on line 120 defines the right sub tree of the root node.
         *
         * So each node recursively splits it symbols into a left and right sub tree and stops
         * when there are only two symbols left in the sub tree. When there is only two symbols
         * left in the branch a leaf node (l) is defined to end the branch.
         *
         * Currently the complete tree is built from scratch on each call. But some of the
         * sub trees are static and does never change. If needed these sub tree could be cached
         * and reused so that they are only built once.
         */

        //@formatter:off
        return
        d(s(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, SPACE, SEND, NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, COMMA, PERIOD, EXCLAMATION_MARK, QUESTION_MARK, BACKSPACE, DELETE_WORD, CLEAR_BUFFER, SETTING), suggestions,
            n(s(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, SPACE, SEND),
                n(s(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P),
                    n(s(A, B, E, F, I, J, M, N),
                        n(s(A, B, E, F),
                            l(A, B),
                            l(E, F)),
                        n(s(I, J, M ,N),
                            l(I, J),
                            l(M, N))),
                    n(s(C, D, G, H, K, L, O, P),
                        n(s(C, D, G, H),
                            l(C, D),
                            l(G, H)),
                        n(s(K, L, O, P),
                            l(K, L),
                            l(O, P)))),
                n(s(Q, R, S, T, U, V, W, X, Y, Z, SPACE, SEND),
                    n(s(Q, R, U, V, Y, Z),
                        n(s(Q, R, U, V),
                            l(Q, R),
                            l(U, V)),
                        l(Y, Z)),
                    n(s(S, T, W, X, SPACE, SEND),
                        n(s(S, T, W, X),
                            l(S, T),
                            l(W, X)),
                        l(SPACE, SEND)))),
            d(s(NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, PERIOD, COMMA, QUESTION_MARK, EXCLAMATION_MARK, BACKSPACE, DELETE_WORD, CLEAR_BUFFER, SETTING), suggestions,
                n(s(NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, PERIOD, COMMA, QUESTION_MARK, EXCLAMATION_MARK, BACKSPACE, DELETE_WORD, CLEAR_BUFFER, SETTING),
                    n(s(NUM_0, NUM_1, NUM_4, NUM_5, NUM_8, NUM_9, PERIOD, COMMA, NUM_2, NUM_3, NUM_6, NUM_7, QUESTION_MARK, EXCLAMATION_MARK, CLEAR_BUFFER, SETTING),
                        n(s(NUM_0, NUM_1, NUM_4, NUM_5, NUM_8, NUM_9, QUESTION_MARK, EXCLAMATION_MARK),
                            n(s(NUM_0, NUM_1, NUM_4, NUM_5),
                                l(NUM_0, NUM_1),
                                l(NUM_4, NUM_5)),
                            n(s(NUM_8, NUM_9, QUESTION_MARK, EXCLAMATION_MARK),
                                l(NUM_8, NUM_9),
                                l(QUESTION_MARK, EXCLAMATION_MARK))),
                        n(s(NUM_2, NUM_3, NUM_6, NUM_7, PERIOD, COMMA, CLEAR_BUFFER, SETTING),
                            n(s(NUM_2, NUM_3, NUM_6, NUM_7),
                                l(NUM_2, NUM_3),
                                l(NUM_6, NUM_7)),
                            n(s(PERIOD, COMMA, CLEAR_BUFFER, SETTING),
                                l(PERIOD, COMMA),
                                l(SETTING, CLEAR_BUFFER)))),
                    l(BACKSPACE, DELETE_WORD)),
                suggestionNode(suggestions)));
        //@formatter:on
    }


    // A empty node used to end a branch
    private static Node empty = new Node(Collections.emptyList(), null, null);


    /**
     * Defines a dynamic node, a node that included suggestions that are not static
     *
     * @param symbols     a list of all the symbols that exists in the sub tree below the node
     * @param suggestions a list of suggestions to include in the node
     * @param left        the left sub tree
     * @param right       the right sub tree
     * @return a tree node
     */
    private static Node d(List<Symbol> symbols, List<String> suggestions, Node left, Node right) {
        List<Object> items = new ArrayList<Object>(symbols);
        items.addAll(suggestions);
        return new Node(items, left, right);
    }


    /**
     * Defines a static node that is always the same, static list of symbols
     *
     * @param symbols a list of all the symbols that exists in the sub tree below the node
     * @param left    the left sub tree
     * @param right   the right sub tree
     * @return a tree node
     */
    private static Node n(List<Symbol> symbols, Node left, Node right) {
        return new Node(new ArrayList<Object>(symbols), left, right);
    }

    /**
     * Creates two leaf nodes from the two symbols provided
     *
     * @param left  the left symbol
     * @param right the right symbol
     * @return a node containing the left and right leaf nodes
     */
    private static Node l(Symbol left, Symbol right) {
        return new Node(Arrays.<Object>asList(left, right), leaf(left), leaf(right));
    }

    /**
     * Creates a single leaf node from the provided symbol
     *
     * @param symbol  the node symbol
     * @return a node containing a single symbol
     */
    private static Node leaf(Symbol symbol) {
        return leaf((Object) symbol);
    }

    /**
     * Creates a single leaf node from the provided item object
     *
     * @param item  the node item
     * @return a node containing a single symbol
     */
    private static Node leaf(Object item) {
        return new Node(Collections.singletonList(item), empty, empty);
    }

    /**
     * Builds sub tree from the provided suggestions
     *
     * @param suggestions a list of suggestions
     * @return a tree of suggestions
     */
    private static Node suggestionNode(List<String> suggestions) {
        return buildSuggestionsTree(suggestions);
    }

    // Recursively build the suggestions sub ree
    private static Node buildSuggestionsTree(List<String> suggestions) {

        if (suggestions.isEmpty()) {
            return empty;
        } else if (suggestions.size() == 1) {
            return leaf(suggestions.get(0));
        } else {
            int mid = suggestions.size() / 2;
            return new SuggestionsNode(suggestions,
                    buildSuggestionsTree(suggestions.subList(0, mid)),
                    buildSuggestionsTree(suggestions.subList(mid, suggestions.size())));
        }
    }

    // Constructs a list from the provided symbols
    private static List<Symbol> s(Symbol... symbols) {
        return Arrays.asList(symbols);
    }


    /**
     * A node in the selection tree
     */
    public static class Node {

        private Node left;
        private Node right;
        private List<Object> items; // All items in the subtree below this node

        public Node(List<Object> items, Node left, Node right) {
            this.left = left;
            this.right = right;
            this.items = items;
        }

        /**
         * Returns true if this node contains the given value
         * <p>
         * The value can currently be a symbol or a suggestion string
         *
         * @param value the value to check for
         * @return true if the value is in the subtree
         */
        public boolean contains(Object value) {
            return items.contains(value);
        }

        /**
         * Returns true if this node only has a single item
         *
         * @return true if the node only has a single item
         */
        public boolean isSingle() {
            return items.size() == 1;
        }

        /**
         * Returns the first item stored in this node
         * <p>
         * This should only be used for single nodes to get access to the symbol or suggestion
         * stored in the node.
         *
         * @return a object representing either a symbol or a suggestion string
         */
        public Object getItem() {
            if (items.size() != 1) {
                throw new UnsupportedOperationException("You can only get the item from a \"single\" node.");
            }
            return items.get(0);
        }

        /**
         * Returns the items in the current node
         *
         * @return a list of objects
         */
        public List<Object> getItems(){
            return items;
        }

        /**
         * Returns the left sub tree
         *
         * @return a tree node
         */
        public Node getLeft() {
            return findNext(left);
        }

        /**
         * Returns the right sub tree
         *
         * @return a tree node
         */
        public Node getRight() {
            return findNext(right);
        }


        private boolean isEmpty() {
            return items.isEmpty();
        }

        /**
         * Used to handle a special case that should only happen with the first suggestions node.
         * The first suggestions node is always in the tree, even if there is no suggestions currently
         * available. If this case is not handled the user will be able to select the empty
         * suggestions tree and will get stuck on the empty node. It will also look weird because
         * when the parent of the suggestions node is active and the suggestions are empty, all
         * the visible items in the layout will be in the left or right child and thus will look
         * the same.
         * <p>
         * To fix, before the next node is returned we look at its left and right branch to
         * see if any of them is empty. If one of its branches are empty we shortcut the
         * selection and immediately jump to the next node's other branch, which should never
         * be empty if the tree is defined correctly.
         *
         * @param next
         * @return
         */
        private Node findNext(Node next) {
            if (next.isSingle()) { // If the next node is a single node we just return it
                return next;
            }
            if (next.left != null && next.left.isEmpty()) {
                return next.right; // The left sub tree of the next node is empty so we return it's right sub tree instead
            } else if (next.right != null && next.right.isEmpty()) {
                return next.left; // The right sub tree of the next node is empty so we return it's left sub tree instead
            }

            return next; // Both the sub trees are non empty so no shortcut is needed.
        }
    }

    /**
     * Node used for suggestions
     */
    public static class SuggestionsNode extends Node {

        public SuggestionsNode(List<String> suggestions, Node left, Node right) {
            super(new ArrayList<Object>(suggestions), left, right);
        }
    }
}
