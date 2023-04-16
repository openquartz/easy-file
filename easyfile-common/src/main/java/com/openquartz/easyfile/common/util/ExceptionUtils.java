package com.openquartz.easyfile.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides utilities for manipulating and examining
 * <code>Throwable</code> objects.</p>
 *
 * @author svnee
 * @since 1.0
 */
public final class ExceptionUtils {

    /**
     * <p>
     * Public constructor allows an instance of <code>ExceptionUtils</code> to be created, although that is not
     * normally necessary.
     * </p>
     */
    private ExceptionUtils() {
        super();
    }

    /**
     * <p>Returns the list of <code>Throwable</code> objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return a list containing
     * one element - the input throwable.
     * A throwable with one cause will return a list containing
     * two elements. - the input throwable and the cause throwable.
     * A <code>null</code> throwable will return a list of size zero.</p>
     *
     * <p>This method handles recursive cause structures that might
     * otherwise cause infinite loops. The cause chain is processed until
     * the end is reached, or until the next item in the chain is already
     * in the result set.</p>
     *
     * @param throwable the throwable to inspect, may be null
     * @return the list of throwables, never null
     * @since 2.2
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * Throw a checked exception without adding the exception to the throws
     * clause of the calling method. This method prevents throws clause
     * pollution and reduces the clutter of "Caused by" exceptions in the
     * stacktrace.
     * <p>
     * The use of this technique may be controversial, but exceedingly useful to
     * library developers.
     * <code>
     * public int propagateExample { // note that there is no throws clause
     * try {
     * return invocation(); // throws IOException
     * } catch (Exception e) {
     * return ExceptionUtils.rethrow(e);  // propagates a checked exception
     * }
     * }
     * </code>
     * <p>
     * This is an alternative to the more conservative approach of wrapping the
     * checked exception in a RuntimeException:
     * <code>
     * public int wrapExample { // note that there is no throws clause
     * try {
     * return invocation(); // throws IOException
     * } catch (Error e) {
     * throw e;
     * } catch (RuntimeException e) {
     * throw e;  // wraps a checked exception
     * } catch (Exception e) {
     * throw new UndeclaredThrowableException(e);  // wraps a checked exception
     * }
     * }
     * </code>
     * <p>
     * One downside to using this approach is that the java compiler will not
     * allow invoking code to specify a checked exception in a catch clause
     * unless there is some code path within the try block that has invoked a
     * method declared with that checked exception. If the invoking site wishes
     * to catch the shaded checked exception, it must either invoke the shaded
     * code through a method re-declaring the desired checked exception, or
     * catch Exception and use the instanceof operator. Either of these
     * techniques are required when interacting with non-java jvm code such as
     * Jython, Scala, or Groovy, since these languages do not consider any
     * exceptions as checked.
     *
     * @param throwable The throwable to rethrow.
     * @param <R> The type of the returned value.
     * @return Never actually returned, this generic type matches any type
     * which the calling site requires. "Returning" the results of this
     * method, as done in the propagateExample above, will satisfy the
     * java compiler requirement that all code paths return a value.
     * @since 3.5
     */
    public static <R> R rethrow(final Throwable throwable) {
        // claim that the typeErasure invocation throws a RuntimeException
        return ExceptionUtils.typeErasure(throwable);
    }

    /**
     * Claim a Throwable is another Exception type using type erasure. This
     * hides a checked exception from the java compiler, allowing a checked
     * exception to be thrown without having the exception in the method's throw
     * clause.
     */
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(final Throwable throwable) throws T {
        throw (T) throwable;
    }

}
