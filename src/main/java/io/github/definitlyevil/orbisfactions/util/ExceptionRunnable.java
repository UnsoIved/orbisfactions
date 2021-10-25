package io.github.definitlyevil.orbisfactions.util;

public interface ExceptionRunnable {

    /**
     * Execute a function which might throw an exception.
     * @return The output of this function.
     * @throws Exception the exception, of course
     */
    Object run() throws Exception;

}
