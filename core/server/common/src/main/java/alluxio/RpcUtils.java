/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio;

import alluxio.exception.AlluxioException;
import alluxio.exception.UnexpectedAlluxioException;
import alluxio.thrift.AlluxioTException;
import alluxio.thrift.ThriftIOException;

import org.slf4j.Logger;

import java.io.IOException;

/**
 * Utilities for handling RPC calls.
 */
public final class RpcUtils {
  /**
   * Calls the given {@link RpcCallable} and handles any exceptions thrown.
   *
   * @param logger the logger to use for this call
   * @param callable the callable to call
   * @param <T> the return type of the callable
   * @return the return value from calling the callable
   * @throws AlluxioTException if the callable throws an Alluxio or runtime exception
   */
  public static <T> T call(Logger logger, RpcCallable<T> callable) throws AlluxioTException {
    try {
      return callable.call();
    } catch (AlluxioException e) {
      logger.debug("Exit (Error): {}", callable, e);
      throw e.toThrift();
    } catch (Exception e) {
      logger.error("Exit (Error): {}", callable, e);
      throw new UnexpectedAlluxioException(e).toThrift();
    }
  }

  /**
   * Calls the given {@link RpcCallableThrowsIOException} and handles any exceptions thrown.
   *
   * @param logger the logger to use for this call
   * @param callable the callable to call
   * @param <T> the return type of the callable
   * @return the return value from calling the callable
   * @throws AlluxioTException if the callable throws an Alluxio or runtime exception
   * @throws ThriftIOException if the callable throws an IOException
   */
  public static <T> T call(Logger logger, RpcCallableThrowsIOException<T> callable)
      throws AlluxioTException, ThriftIOException {
    try {
      return callable.call();
    } catch (AlluxioException e) {
      logger.debug("Exit (Error): {}", callable, e);
      throw e.toThrift();
    } catch (IOException e) {
      logger.debug("Exit (Error): {}", callable, e);
      throw new ThriftIOException(e.getMessage());
    } catch (Exception e) {
      logger.error("Exit (Error): {}", callable, e);
      throw new UnexpectedAlluxioException(e).toThrift();
    }
  }

  /**
   * Calls the given {@link RpcCallable} and handles any exceptions thrown. The callable should
   * implement a toString with the following format: "CallName: arg1=value1, arg2=value2,...".
   * The toString will be used to log enter and exit information with debug logging is enabled.
   *
   * @param logger the logger to use for this call
   * @param callable the callable to call
   * @param <T> the return type of the callable
   * @return the return value from calling the callable
   * @throws AlluxioTException if the callable throws an Alluxio or runtime exception
   */
  public static <T> T callAndLog(Logger logger, RpcCallable<T> callable) throws AlluxioTException {
    logger.debug("Enter: {}", callable);
    T ret = call(logger, callable);
    logger.debug("Exit (OK): {}", callable);
    return ret;
  }

  /**
   * Calls the given {@link RpcCallableThrowsIOException} and handles any exceptions thrown. The
   * callable should implement a toString with the following format:
   * "CallName: arg1=value1, arg2=value2,...". The toString will be used to log enter and exit
   * information with debug logging is enabled.
   *
   * @param logger the logger to use for this call
   * @param callable the callable to call
   * @param <T> the return type of the callable
   * @return the return value from calling the callable
   * @throws AlluxioTException if the callable throws an Alluxio or runtime exception
   * @throws ThriftIOException if the callable throws an IOException
   */
  public static <T> T callAndLog(Logger logger, RpcCallableThrowsIOException<T> callable)
      throws AlluxioTException, ThriftIOException {
    logger.debug("Enter: {}", callable);
    T ret = call(logger, callable);
    logger.debug("Exit (OK): {}", callable);
    return ret;
  }

  /**
   * An interface representing a callable which can only throw Alluxio exceptions.
   *
   * @param <T> the return type of the callable
   */
  public interface RpcCallable<T> {
    /**
     * The RPC implementation.
     *
     * @return the return value from the RPC
     * @throws AlluxioException if an expected exception occurs in the Alluxio system
     */
    T call() throws AlluxioException;
  }

  /**
   * An interface representing a callable which can only throw Alluxio or IO exceptions.
   *
   * @param <T> the return type of the callable
   */
  public interface RpcCallableThrowsIOException<T> {
    /**
     * The RPC implementation.
     *
     * @return the return value from the RPC
     * @throws AlluxioException if an expected exception occurs in the Alluxio system
     * @throws IOException if an exception is thrown when interacting with the underlying system
     */
    T call() throws AlluxioException, IOException;
  }

  private RpcUtils() {} // prevent instantiation
}
