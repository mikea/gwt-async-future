package com.googlecode.gwtasync.util;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Pair<A, B> {
  public final A first;
  public final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<A, B>(a, b);
  }
}
