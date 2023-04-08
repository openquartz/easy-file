package com.openquartz.easyfile.common.bean;

import java.util.Objects;

/**
 * Pair
 *
 * @param <L> L
 * @param <R> R
 * @author svnee
 */
public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Gets the left element from this pair.</p>
     *
     * <p>When treated as a key-value pair, this is the key.</p>
     *
     * @return the left element, may be null
     */
    public L getLeft() {
        return left;
    }

    /**
     * <p>Gets the right element from this pair.</p>
     *
     * <p>When treated as a key-value pair, this is the value.</p>
     *
     * @return the right element, may be null
     */
    public R getRight() {
        return right;
    }

    /**
     * <p>Gets the key from this pair.</p>
     *
     * <p>This method implements the {@code Map.Entry} interface returning the
     * left element as the key.</p>
     *
     * @return the left element as the key, may be null
     */
    public L getKey() {
        return getLeft();
    }

    /**
     * <p>Gets the value from this pair.</p>
     *
     * <p>This method implements the {@code Map.Entry} interface returning the
     * right element as the value.</p>
     *
     * @return the right element as the value, may be null
     */
    public R getValue() {
        return getRight();
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Compares this pair to another based on the two elements.</p>
     *
     * @param obj the object to compare to, null returns false
     * @return true if the elements of the pair are equal
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Pair) {
            Pair pair = (Pair) obj;
            return Objects.equals(getKey(), pair.getKey())
                && Objects.equals(getValue(), pair.getValue());
        }
        return false;
    }

    /**
     * <p>Returns a suitable hash code.
     * The hash code follows the definition in {@code Map.Entry}.</p>
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        // see Map.Entry API specification
        return (getKey() == null ? 0 : getKey().hashCode()) ^
            (getValue() == null ? 0 : getValue().hashCode());
    }

    /**
     * <p>Returns a String representation of this pair using the format {@code ($left,$right)}.</p>
     *
     * @return a string describing this object, not null
     */
    @Override
    public String toString() {
        return "(" + getLeft() + ',' + getRight() + ')';
    }

    /**
     * <p>Formats the receiver using the given format.</p>
     *
     * <p>This uses {@link java.util.Formattable} to perform the formatting. Two variables may
     * be used to embed the left and right elements. Use {@code %1$s} for the left
     * element (key) and {@code %2$s} for the right element (value).
     * The default format used by {@code toString()} is {@code (%1$s,%2$s)}.</p>
     *
     * @param format the format string, optionally containing {@code %1$s} and {@code %2$s}, not null
     * @return the formatted string, not null
     */
    public String toString(final String format) {
        return String.format(format, getLeft(), getRight());
    }

}
