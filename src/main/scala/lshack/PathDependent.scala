package lshack

/**
 * @author <a href="mailto:koleman@livesafemobile.com">Koleman Nix</a>
 */
object PathDependent {

  trait Foo {
    trait Bar
    def bar: Bar
  }

  def foo(f: Foo): f.Bar = f.bar

}
